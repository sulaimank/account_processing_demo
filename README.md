Magic Eden Assessment
=


Instructions on how to run and test code
-

At root of pom.xml, build the jar
1) mvn package

cd to target where maven builds jar
1) java -classpath magiceden-1.0-SNAPSHOT-jar-with-dependencies.jar  magicEden.DynamicLoadDriver

Run all JUnit test cases
1) mvn test


Design Pattern Discussion
-
As I architected a solution I kept in mind time constraints but also in keeping with the spirit that you
wanted production-level real-time system that emulates the indexing of data on the blockchain.  I added enough
error checks and handling to ensure that errors are logged quickly.

I used Gson library which is a Google library that allows for parsing json
files based on a POJO class.  The Account class is the POJO that as I read in an array of accounts.

Since the supplied JSON file contains data, I created a robust JUnit file that in the setup, loads the JSON
array into memory.  Then each account element was handled in a continuous uniform and random distribution
between 0 - 1 second.  This allows test cases to be run in a way that simulates production loads and threading
issues show up early in the test cases.

I created some JUnit test cases that handle both scenarios provided and also I added test cases to test certain
conditions.  For example, I noticed on account did not have a version which was causing a NPE, so in the getter 
I default to 0.  I added a test case for that and made sure the NPE did not get generated.

I am also assuming that same account records (same account id) with different versions are being processed in
order.  This might not be the case in production.  So I would create a PriorityQueue sorted by version.  


In development, I would work off of feature branches that match a story.  And then I would push the PR to GitHub and
have another person (sometimes multiple people for complex stories) code review the PR.  Once approved, I would merge
the PR into master.  I would have a GitHub and Jenkins hook so that Jenkins would build a new jar and run all test cases.
Once all test cases pass, Jenkins would deploy the JAR info production.

In production, the main processor would listen for messages (either on a messaging system like Kafka or thru a REST call)
and process the message.

Observability & Monitor to add to a production system
-




