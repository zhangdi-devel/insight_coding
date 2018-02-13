# Coding challenge for Insight data engineering workshop



## 1. Dependencies

 `scala` is used for this project. 

- Compiling and running
  - `sbt-1.x` is used for compiling and managing the library dependencies.
  - `java-1.8` is used for running the jar file. 
- Library dependencies (`sbt` will handle the libraries. No need to download separately)
  - `scalatest` is used for unit testing.
  -  `slf4j` and `log4j` backend are used for logging. 

## 2. Run instructions

The script `run.sh` will first try to compile the project and make an assembly jar file with dependencies using `sbt`, and then run the jar file using `java`. In case the compiling is not successful, it will fall back to use the jar file that was pre-compiled.

## 3. Approach

The input are read and processed line by line. Two open addressing (closed hashing) hash tables are maintained during the processing. One (`DonorRepository`) stores the earliest year of donation for each donor. The other (`Bank`) stores the donations (`Account`) from repeated donors for each recipient in each zipcode and year. Importantly, the donations of one `Account` are stored in a black-red tree with an augmented count field which stores the size of the subtree. With this balanced binary search tree, it takes $O(log(n))$ time for both insertion and looking-up for the $k_{th}$ element. The source code is explained in detail below.

- `DonationAnalytics` is the entry point of the program. It reads and process transactions line by line.If a transaction is valid, it will be used to update the`DonorRepository`. If the donor has donated before, the transaction will be used to update the `Bank` . Output are available in real time (well, there is still some delay caused by file IO bufferring).


- `Transaction` is the class to hold the information we need from an input line. The constructor takes an input line, splits it with '|', and uses regular expressions to extract the necessary information. Invalid input lines will be ignored. Valid transactions will be used to update the `DonorRepository`. If the donor is considered as repeated, the transaction will be used to update the `Bank`.


- `DonorRepository` is the class to hold the earliest donation year for each donor. It has an `update` method, which takes an transaction and update the earliest donation year if possible. This method will return true if the donor has donated in any prior year, and false otherwise.


- `Bank` is the class to hold the accounts for each recipient in each area (zipcode) and year. The key of the hash table is `s"$recipient|$zipcode|$year"`, the value is an `Account` which consists of a total amount and an `OrderStatisticTree`. The `update` method takes an transaction and update the account for the key by adjusting the total amount and inserting the new amount to the `OrderStatisticTree`. The `lookup` method takes the information for the key and a percent number (between 0 and 1). The percent will be convert to $k=size * percent$ and then the $k_{th}$smallest element in the tree will be returned.
- `OrderStatisticTree` is the class to hold the donations for each `Account` in the `Bank`. It is a red-black tree augmented with a count field that stores the size of the subtree, so that the time of the query for the $k_{th}$ element becomes $O(log(n))$.

## 4. Performance and scalability

**Space complexity**

Transactions are processed line by line, so they only take a very small constant space in memory. It is the two hash tables that will take large amount of space, especially when the input file size is large. 

- *Under reasonable assumputions*

  We have $n$ input lines and most of the input are valid. The lines come earlier have an earlier transaction date. Everyone donate at most once a year. Under these assumptions, a record is either a new donor or a repeated donor. Note that every new donor will result in a new entry in the `DonorRepository` hash, and every repeated donor will result in a new entry in a `OrderStatisticTree` of the `Bank` hash. The total space complexity is $O(n)$.

- *Violation of the assumptions*

  If some of the assumptions are violated, it will only result in less space usage. If some input line are not valid transactions, they will be ignored. If a late transaction came earlier than the first transaction, we wouldn't know it should be repeated donation, so it will be absent in the `Bank`. If a donor donate multiple times in a year, only one record will be stored in the `DonorRepository`.

In summary, the space complexity is $O(n)$. 

**Time complexity**

Because of using hash tables, I will focus on the average time complexity. The average time required to update the `DonorRepository` is $O(n)$. The average time required to update the `Bank` is $O(\sum \frac{a}{n} \times log(a))$ where $a$ is the number of donations of an account, and $n$ is the total number of repeated donations. So the overall time complexity depends on how repeated donations are distributed in the accounts. In the worst case where every repeated donation is from one area (zipcode), in one year, and for one recipient, i.e. in one account, we will have $O(n \times log(n))$ time complexity. In the best case, every account has one donation, we will have $O(n)$ time complexity. The reality lies somewhere between these two extreme cases. It is observed from the Federal Election Commission (FEC) data that most accounts have very small $a$ (< 100), but a few accounts can have relatively large $a$ (> 10000). 

**Testing with FEC datasets**

All the individual donation record files from 1979 to 2018 are downloaded and combined chronologically. There are over 51,000,000 records in the 8.1 GB combined file. The testing is performed on a Mac (CPU Intel i5, 8G memory, fusion disk), with java maximum heap size set to 6g. It took about 5.5 minutes to process the whole dataset (~155k records/s, ~24.5 MB/s). There are about 12.0 million donors, 5.1 million accounts and 18.5 million repeated donations. Below is a histogram of running time (one `+` for one second) for every 2 million records. It shows that the running time scales very well even at later stage when the trees became larger. The two peaks are likely due to garbege collection.

```Bash
       1- 2000000: +++++++++++
 2000001- 4000000: +++++++++++++
 4000001- 6000000: +++++++++++
 6000001- 8000000: +++++++++++++
 8000001-10000000: ++++++++++++
10000001-12000000: +++++++++++
12000001-14000000: +++++++++++++++
14000001-16000000: +++++++++++++
16000001-18000000: ++++++++++++
18000001-20000000: +++++++++++++++
20000001-22000000: ++++++++++++++++++++++++++++++
22000001-24000000: +++++++++++++
24000001-26000000: ++++++++++
26000001-28000000: ++++++++++
28000001-30000000: ++++++++++++++++
30000001-32000000: ++++++++++
32000001-34000000: ++++++++++
34000001-36000000: ++++++++++
36000001-38000000: +++++++++++
38000001-40000000: +++++++++++
40000001-42000000: ++++++++++
42000001-44000000: +++++++++++++++++++++
44000001-46000000: +++++++++++
46000001-48000000: ++++++++++++
48000001-50000000: ++++++++++++
```



