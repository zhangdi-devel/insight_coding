# Coding challenge for Insight data engineering workshop

[TOC]

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

The input are read and processed line by line. Two hash tables are maintained during the process. One (`DonorRepository`) stores the earliest year of donation for each donor. The other (`Bank`) stores the donations (`Account`) from repeated donors for each recipient in each zipcode and year. Importantly, the donations are stored in a black-red tree with an augmented count field which stores the size of the subtree. With this balanced binary search tree, it takes $O(log(n))$ time for both insertion and looking-up for the $k_{th}$ element. The source code is explained in detail below.

- `DonationAnalytics` is the entry point of the program. It reads and process transactions line by line.If a transaction is valid, it will be used to update the`DonorRepository`. If the donor has donated before, the transaction will be used to update the `Bank` . Output are available in real time (well, there is still some delay caused by file IO bufferring).


- `Transaction` is the class to hold the information we need from an input line. The constructor takes an input line, splits it with '|', and uses regular expressions to extract the necessary information. Invalid input lines will be ignored. Valid transactions will be used to update the `DonorRepository`. If the donor is considered as repeated, the transaction will be used to update the `Bank`.


- `DonorRepository` is the class to hold the earliest donation year for each donor. It has an `update` method, which takes an transaction and update the earliest donation year if possible. This method will return true if the donor has donated in any prior year, and false otherwise.


- `Bank` is the class to hold the accounts for each recipient in each area (zipcode) and year. The key of the hash table is `s"$recipient|$zipcode|$year"`, the value is an `Account` which consists of a total amount and a `RedBlackTree`. The `update` method takes an transaction and update the account for the key by adjusting the total amount and inserting the new amount to the `RedBlackTree`. The `lookup` method takes the information for the key and a percent number (between 0 and 1). The percent will be convert to $k=size * percent$ and then the $k_{th}$smallest element in the tree will be returned.
- `RedBlackTree` is the class to hold the donations for each `Account` in the `Bank`. It is augmented with a count field that stores the size of the subtree, so that the time of the query for the $k_{th}$ element becomes $O(log(n))$.

## 4. Performance and scalability

**Space complexity**

Transactions are processed line by line, so they only take a very small constant space in memory. It is the two hash tables that will take large amount of space, especially when the input file size is large. 

- *Under reasonable assumputions*

  We have $n$ input lines and most of the input are valid. The lines come earlier have an earlier transaction date. Everyone donate at most once a year. Under these assumptions, a record is either a new donor or a repeated donor. Note that every new donor will result in a new entry in the `DonorRepository` hash, and every repeated donor will result in a new entry in a `RedBlackTree` of the `Bank` hash. The total space complexity is $O(n)$.

- *Violation of the assumptions*

  If some of the assumptions are violated, it will only result in less space usage. If some input line are not valid transactions, they will be ignored. If a late transaction came earlier than the first transaction, we wouldn't know it should be repeated donation, so it will be absent in the `Bank`. If a donor donate multiple times in a year, only one record will be stored in the `DonorRepository`.

In summary, the space complexity is $O(n)$. 

**Time complexity**

Because of using hash tables, I will focus on the average time complexity. The average time required to update the `DonorRepository` is $O(n)$. The average time required to update the `Bank` is $O(n\times log(a))$ where a is the size of the account. 