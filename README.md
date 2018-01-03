# TicketService
A simple ticket service that facilitates the discovery, temporary hold, and final reservation of seats within a high-demand performance venue.

Ticket Service Coding Challenge

Implement a simple ticket service that facilitates the discovery,
temporary hold, and final reservation of seats within a high-demand
performance venue.

Your homework assignment is to design and write a Ticket Service
that provides the following functions:

Find the number of seats available within the venue.
Note: available seats are seats that are neither held nor reserved.

Find and hold the best available seats on behalf of a customer
Note: each ticket hold should expire within a set number of seconds.

Reserve and commit a specific group of held seats for a customer.

Requirements

The ticket service implementation should be written in Java.
The solution and tests should build and execute entirely
via the command line using either Maven or Gradle as the build tool.
A README file should be included in your submission that documents
your assumptions and includes instructions for building the solution
and executing the tests.
Implementation mechanisms such as disk-based storage,
a REST API, and a front-end GUI are not required.

-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

Assumptions

This application would live within a larger venue management system.
A play might run for several weeks. So a ticket purchase would include
a specific day, and perhaps even a time (some shows have matinee and
evening performances on the same day). So the interface to be implemented
in this homework assignment might rightly be called a "performance."

Multiple servers might invoke the interface concurrently. So the
implementation must be thread safe. But each operation (hold, reserve)
only takes a few milliseconds at most, and the number of operations
per day for a given performance might be numbered in the dozens.
Certainly not millions. So coarse synchronization would be more than
sufficient, and would be simplest (one big lock at the top).

This implementation's unit tests provide 100% code coverage. Because
of this, and the lack of dependencies on untrusted/unreliable/untested
external code, mocks are not used. Each class's dependent classes are
tested and trusted, and therefore can be used in unit tests of other
higher level classes that use them.


Evaluation

The interested evaluator should create a directory and cd into it,
then download the following two repositories:

https://github.com/AirPuckey/TicketService.git

https://github.com/AirPuckey/VenueViewer.git


`mkdir RonaldHughesEvaluation; cd RonaldHughesEvaluation`

`git clone https://github.com/AirPuckey/TicketService.git`

`git clone https://github.com/AirPuckey/VenueViewer.git`

Build them.

`cd TicketService && ./gradlew jar && cd ..`

`cd VenueViewer && ./gradlew jar && cd ..`

Copy the resulting jar files into the current directory,
giving them more palatable names.

`cp TicketService/build/libs/ticketservice-1.0-SNAPSHOT.jar ticketService.jar`

`cp VenueViewer/build/libs/VenueViewer-1.0-SNAPSHOT.jar viewer.jar`

Then execute the following command:

`java -cp ticketService.jar RunMe -ticketService | java -cp viewer.jar RunMe`

That's it! To see the various options, run either jar in the same way
with the `-usage` flag.

If your platform doesn't support UNIX's command pipeline mechanism,
these programs support `-outputFile` and `-inputFile` options. You know
what to do.