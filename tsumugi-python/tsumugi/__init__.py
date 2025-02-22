import sys
from pathlib import Path

proto_root = Path(__file__).parent.joinpath("proto")
if str(proto_root.absolute()) not in sys.path:
    sys.path.append(str(proto_root.absolute()))

# We must me sure that all the protobuf packages are loaded!
# An order is also important!
from tsumugi.proto import repository_pb2  # noqa: E402, F401
from tsumugi.proto import analyzers_pb2  # noqa: E402, F401
from tsumugi.proto import strategies_pb2  # noqa: E402, F401
