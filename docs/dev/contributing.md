# Contributing guide

## Development environment

This section provides a brief overview of the minimal developer environment. Because `tsumugi` is a complex multi-language project, this part is split into subsections covering the JVM server component, the protobuf and buf component, and the Python client component.

### Makefile

A portion of useful commands has been moved to the `Makefile`. To run `make` commands, one needs to download and install the make utility. The make utility can be obtained from the [GNU Make website](https://www.gnu.org/software/make/#download). For some operating systems, `make` is also available through package managers.

### JVM

For managing Java, Scala, and Maven, it is strongly recommended to use tools like [SDKMan](https://sdkman.io/). With SDKMan, all the necessary components can be easily installed using just a few shell commands.

- Java 11; [OpenJDK archieve](https://jdk.java.net/archive/), [installation manual](https://openjdk.org/install/);
- Apache Maven; [Installation manual](https://maven.apache.org/install.html);
- Scala 2.12.X; [Installation manual](https://www.scala-lang.org/download/);

Installation with `SDKMan`:

```sh
  sdk install java 11.0.25-zulu
  sdk install maven 3.9.6
  sdk install scala 2.12.20
```


### Protobuf

- [Protoc](https://github.com/protocolbuffers/protobuf); [Installation manual](https://grpc.io/docs/protoc-installation/);
- [Buf](https://github.com/bufbuild/buf); [Installation manual](https://buf.build/docs/installation/);

Java classes are generated from the protobuf messages during the build via maven plugin and there is no reason to store them inside the repository. Python classes are generated from the protobuf messages manually. To generat or update Python classes run the following:

```sh
  make generate_code
```

### Python

It is strongly recommend to use tools like [pyenv](https://github.com/pyenv/pyenv/tree/master) to manage Python versions.

- [Python](https://www.python.org/downloads/release/python-3100/); with `pyenv`: `pyenv install 3.10`;
- [Poetry](https://python-poetry.org/); [Installation manual](https://python-poetry.org/docs/#installation);

Run the following command to create a python venv:

```sh
  poetry env use %path-to-python3.10% # On POSIX systems it should be like ~/.pyenv/versions/3.10.14/bin/python
  poetry install --with dev
```

## Code style and naming conventions

### Linters

Tsumugi uses [`scalafmt`](https://scalameta.org/scalafmt/) for server part and [`ruff`](https://github.com/astral-sh/ruff) for Python client.

To apply formatting rules to scala part run the following command:

```sh
  mvn spotless:apply
```

To apply python formatting rules run the following command:

```sh
  make lint_python
```

### Python tips

All the APIs built on top of ptorobuf classes are done in the following way:

The protobuf message:

```proto3
  message KLLParameters {
    int32 sketch_size = 1;
    double shrinking_factor = 2;
    int32 number_of_buckets = 3;
  }
```

The Python API:

```python
  @dataclass
  class KLLParameters:
    """Parameters for KLLSketch."""

    sketch_size: int
    shrinking_factor: float
    number_of_buckets: int

    def _to_proto(self) -> proto.KLLSketch.KLLParameters:
        return proto.KLLSketch.KLLParameters(
            sketch_size=self.sketch_size,
            shrinking_factor=self.shrinking_factor,
            number_of_buckets=self.number_of_buckets,
        )
```

To maintain a consistent style across all Python code, the following rules should be followed:

1. All classes that wrap code generated from protobuf messages should be implemented as dataclasses, unless there is a compelling reason not to do so.
2. Each of these classes should have a private method `_to_proto(self) -> "protobuf class"` that converts the dataclass to the proto-serializable class.

## Runnign examples or testing clients

To simplify testing and development, there is a script that builds a server plugin, downloads and unpacks the Spark distribution, and runs the Spark Connect Server with all the necessary configurations. To run it, use `make run_spark_server`. After that, the server will be available at `sc://localhost:15002`.

## Examples and notebooks

Examples and Notebooks are part of the documentation and are placed in `docs/notebooks` folder.
