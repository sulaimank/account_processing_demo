Magic Eden Assessment
=


Instructions on how to run and test code
-

At root of pom.xml, install jar dependencies and build the jar containing all dependencies

1) mvn install
2) mvn package

To run the driver's main class cd to target where maven builds the jar

1) java -classpath magiceden-1.0-SNAPSHOT-jar-with-dependencies.jar magicEden.DynamicLoadDriver

Run all JUnit test cases

1) mvn test

Design Pattern Discussion
-
As I architected a solution I kept in mind time constraints but also in keeping with the spirit that you
wanted production-level real-time system that emulates the indexing of data on the blockchain. I added enough
error checks and handling to ensure that errors are logged quickly.

I used Gson library which is a Google library that allows for parsing json
files based on several POJO classes that represent the JSON structure. The Account class is the POJO that as I read in
an array of accounts.

Since the supplied JSON file contains data, I created a robust JUnit file that in the setup, loads the JSON
array into memory. Then each account element was handled in a continuous uniform and random distribution
between 0 - 1 second. This allows test cases to be run in a way that simulates production loads and threading
issues show up early in the test cases.

I created some JUnit test cases that handle both scenarios provided and also I added test cases to test certain
conditions. For example, I noticed on account did not have a version which was causing a NPE, so in the getter
I default to 0. I added a test case for that and made sure the NPE did not get generated.

I am also assuming that same account records (same account id) with different versions are being processed in
order. This might not be the case in production. So I would create a PriorityQueue sorted by version so I don't have to
iterate
the account list to find the greatest version number.

In development, I would work off of feature branches that match a story. And then I would push the PR to GitHub and
have another person (sometimes multiple people for complex stories) code review the PR. Once approved, I would merge
the PR into master. I would have a GitHub and Jenkins hook so that Jenkins would build a new jar and run all test cases.
Once all test cases pass, Jenkins would deploy the JAR info production.

In production, the main processor would listen for messages (either on a messaging system like Kafka or thru a REST
call)
and process the message.

Architecture tradeoffs
-
I pass a reference to the runnable to Account instead of keeping track of a Future token when submitting the job
to the thread pool. It is cleaner in my opinion.

Also, I have one Data class that manages the data for the various account types. There is no check to ensure
that the right data fields are populated for the right account type. I would use a custom data deserializer to
handle polymorphic deserialization.

I would also add more handling to handle cases where Account data is corrupt or arrives in different orders.

If performance is an issue I would use Kafka queues with each queue handling a particular account type.

Observability & Monitor to add to a production system
-
I would monitor the thread pool and adjust either the thread pool strategy (ie. bounded thread pool vs cached). I would
also monitor system and memory load on the instance running the process.

Logs are equally important. I integrated log4j and used that when testing how the threads behave in the thread pool. The
thread id is logged. It can give you clues as how the thread pool can handle multiple threads


Some questions / thoughts
-
If a message is being processed and a newer message comes in (same message id) but with different data payload,
if we cancel the older one, we don't process the older message's data.  

