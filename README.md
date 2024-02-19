# Heap Heap Hooray - MiniJava Garbage Collector

## Technical Overview:

### Garbage Collection Algorithms:

    Reference Counting:
        Relies on maintaining reference counts for objects.
        Suitable for scenarios with simple object lifetimes but suffers from cycles and overhead.

    Mark-Sweep:
        Involves marking reachable objects and sweeping away unreachable ones.
        More efficient than reference counting but can lead to fragmentation.

    Copying:
        Involves managing two independent heaps.
        During garbage collection, live allocations are copied to a new heap, defragmenting it in the process.
        Requires a minimal heap system for better control over memory allocation.

    Generational:
        Involves managing multiple independent heaps.
        Based on the idea that most objects die young.
        Involves segregating objects based on age and applying different garbage collection strategies to each generation.

### System Design:

    Integration with MiniJava Compiler:
        Utilizes a support library provided by Appel for the course.
        Integration with a minimal C runtime for memory allocation.

    Heap Management:
        Developing a minimal wrapper over standard memory allocation functions for better control.
        Required for implementing copying garbage collection and defragmentation steps.

    Compiler Flag/Configuration:
        Designing user-friendly methods for setting compiler flags and configurations.
        Extending compiler functionality to support setting garbage collection method.

### Testing and Evaluation:

    Test Suites:
        Writing comprehensive test suites to evaluate different garbage collection methods.
        Testing across various MiniJava programs to gather performance metrics.

    Gathering Metrics:
        Compiling test results to analyze and compare the performance of different garbage collection methods.
        Key metrics include memory usage, execution time, and program efficiency.

### Acknowledgments:

    Dr. Ryan Stansifer for guidance and support throughout the project.
    Ian Orzel and Dylan McDougall for the Jabberwocky SPARC environment.
    Contributors to the MiniJava compiler and related tools.

### How to use:
    Install Poetry from [here](https://python-poetry.org/docs/#installing-with-the-official-installer)
    Install Jabberwocky from [here](https://github.com/Kippiii/jabberwocky-container-manager/tree/4d2cd4380169a0b81d623fc6a7e900807e9c7ffe?tab=readme-ov-file#jabberwocky-container-manager) to get Jabberwocky set up.
    Install the SPARK container from <DROPBOX LINK TO BE ADDED>
