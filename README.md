# Fake

Fake build system

Brother project of [fake-cpp](https://github.com/BurnBirdX7/jetbrains_fake_cpp).

## Use

Execute `fake <task> [tasks...]` in directory with `fake.yaml`.

`fake.yaml` contains description of tasks in following format:
 * task-name
   * "`run`": command that is executed when target is out of date
   * "`dependencies`" (optional): contains **list** of dependencies\
     If **dependencies** not specified, task will be executed if `target` file doesn't exist
   * "`target`" (optional): contains name of target file that will be produced
    after successful execution of this task\
    If **target** isn't specified, task is executed unconditionally

Some details:
 * If at least one task in `fake.yaml` is *malformed* - **fake** fails
   * A task considered *malformed* when non-optional property is undefined or defined incorrectly,
   or not listed property is defined
 * If task listed in dependencies isn't in `fake.yaml`, **fake** will try to find file with the same name
   * If file cannot be found, **fake** remembers this as error and continues processing of the dependencies.
     **fake** will fail at the end.\
     This behaviour allows to detect multiple missing dependencies with one execution of **fake**.
 * If there's a cyclic dependency (`task1 -> task2 -> task3 -> task1`). **fake** immediately fails
 * When **fake** fails, list of occurred errors is printed.

Example:
```yaml
# fake.yaml
compile:
  dependencies: main.cpp
  target: main.o
  run: g++ -c main.cpp -o main.o

build:
  dependencies:
    - compile
  target: main
  run: g++ main.o -o main

exec:
  dependencies:
    - build
  run: ./main
```

```shell
# Shell
~/my_project$ fake exec  # Will execute all tasks
~/my_project$ fake exec  # Will execute only `exec`

~/my_project$ rm ./main
~/my_project$ fake exec  # Will execute `compile` and `exec`

~/my_project$ rm ./main.o
~/my_project$ fake exec  # Will execute all tasks
```

## Build and test

If you run
```shell
./gradlew build
```

it will do pretty much everything.... it will **build** the application it will run **unit-tests**
and will also produce distributable archives.

Distributable archives are placed in `build/distributions` directory.
Each archive contains `fake-1.0/lib` directory that contains the program compiled into .jar library and dependencies,
and `fake-1.0/bin` that contains shell and batch scripts that launch the application.

You can run **fake** from build directory with:
```shell
./gradlew run --args [args] 
```

You can install **fake** on your system by unpacking one of distribution into directory which listed in your `PATH`
(`bin` directory of the archive needs to be in `PATH`).
Or unpack elsewhere and add `[unpack-location]/fake-1.0/bin` to your `PATH`.

Or you can run:
```shell
./gradlew installDist
```
And add `[project root]/build/install/fake/bin` to your `PATH`.

If **fake** is listed in your `PATH` you can execute anywhere with
```shell
fake [tasks]
```

### Useful Gradle tasks
```shell
./gradlew test     # Launches tests
./gradlew build    # Builds, archives, tests
./gradlew distZip  # Creates zip-archive for distribution
./gradlew distTar  # Creates tar-archive for distribution
./gradlew jar      # Creates .jar lib (located in build/lib)
```
