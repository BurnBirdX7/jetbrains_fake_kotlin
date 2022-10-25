# Fake

Fake build system

Brother project of [fake-cpp](https://github.com/BurnBirdX7/jetbrains_fake_cpp). Behaves a bit differently.

## Using

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
