# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# NO CHECKED-IN PROTOBUF GENCODE
# source: analyzers.proto
# Protobuf Python Version: 5.27.1
"""Generated protocol buffer code."""

from google.protobuf import descriptor as _descriptor
from google.protobuf import descriptor_pool as _descriptor_pool
from google.protobuf import runtime_version as _runtime_version
from google.protobuf import symbol_database as _symbol_database
from google.protobuf.internal import builder as _builder

_runtime_version.ValidateProtobufRuntimeVersion(
    _runtime_version.Domain.PUBLIC, 5, 27, 1, "", "analyzers.proto"
)
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(
    b'\n\x0f\x61nalyzers.proto\x12\x19io.mrpowers.tsumugi.proto"\xdb\x10\n\x08\x41nalyzer\x12\x64\n\x15\x61pprox_count_distinct\x18\x01 \x01(\x0b\x32..io.mrpowers.tsumugi.proto.ApproxCountDistinctH\x00R\x13\x61pproxCountDistinct\x12T\n\x0f\x61pprox_quantile\x18\x02 \x01(\x0b\x32).io.mrpowers.tsumugi.proto.ApproxQuantileH\x00R\x0e\x61pproxQuantile\x12W\n\x10\x61pprox_quantiles\x18\x03 \x01(\x0b\x32*.io.mrpowers.tsumugi.proto.ApproxQuantilesH\x00R\x0f\x61pproxQuantiles\x12K\n\x0c\x63olumn_count\x18\x04 \x01(\x0b\x32&.io.mrpowers.tsumugi.proto.ColumnCountH\x00R\x0b\x63olumnCount\x12M\n\x0c\x63ompleteness\x18\x05 \x01(\x0b\x32\'.io.mrpowers.tsumugi.proto.CompletenessH\x00R\x0c\x63ompleteness\x12G\n\ncompliance\x18\x06 \x01(\x0b\x32%.io.mrpowers.tsumugi.proto.ComplianceH\x00R\ncompliance\x12J\n\x0b\x63orrelation\x18\x07 \x01(\x0b\x32&.io.mrpowers.tsumugi.proto.CorrelationH\x00R\x0b\x63orrelation\x12Q\n\x0e\x63ount_distinct\x18\x08 \x01(\x0b\x32(.io.mrpowers.tsumugi.proto.CountDistinctH\x00R\rcountDistinct\x12\x45\n\ncustom_sql\x18\t \x01(\x0b\x32$.io.mrpowers.tsumugi.proto.CustomSqlH\x00R\tcustomSql\x12\x42\n\tdata_type\x18\n \x01(\x0b\x32#.io.mrpowers.tsumugi.proto.DataTypeH\x00R\x08\x64\x61taType\x12M\n\x0c\x64istinctness\x18\x0b \x01(\x0b\x32\'.io.mrpowers.tsumugi.proto.DistinctnessH\x00R\x0c\x64istinctness\x12>\n\x07\x65ntropy\x18\x0c \x01(\x0b\x32".io.mrpowers.tsumugi.proto.EntropyH\x00R\x07\x65ntropy\x12Q\n\x0e\x65xact_quantile\x18\r \x01(\x0b\x32(.io.mrpowers.tsumugi.proto.ExactQuantileH\x00R\rexactQuantile\x12\x44\n\thistogram\x18\x0e \x01(\x0b\x32$.io.mrpowers.tsumugi.proto.HistogramH\x00R\thistogram\x12\x45\n\nkll_sketch\x18\x0f \x01(\x0b\x32$.io.mrpowers.tsumugi.proto.KLLSketchH\x00R\tkllSketch\x12\x45\n\nmax_length\x18\x10 \x01(\x0b\x32$.io.mrpowers.tsumugi.proto.MaxLengthH\x00R\tmaxLength\x12>\n\x07maximum\x18\x11 \x01(\x0b\x32".io.mrpowers.tsumugi.proto.MaximumH\x00R\x07maximum\x12\x35\n\x04mean\x18\x12 \x01(\x0b\x32\x1f.io.mrpowers.tsumugi.proto.MeanH\x00R\x04mean\x12\x45\n\nmin_length\x18\x13 \x01(\x0b\x32$.io.mrpowers.tsumugi.proto.MinLengthH\x00R\tminLength\x12>\n\x07minimum\x18\x14 \x01(\x0b\x32".io.mrpowers.tsumugi.proto.MinimumH\x00R\x07minimum\x12]\n\x12mutual_information\x18\x15 \x01(\x0b\x32,.io.mrpowers.tsumugi.proto.MutualInformationH\x00R\x11mutualInformation\x12N\n\rpattern_match\x18\x16 \x01(\x0b\x32\'.io.mrpowers.tsumugi.proto.PatternMatchH\x00R\x0cpatternMatch\x12L\n\rratio_of_sums\x18\x17 \x01(\x0b\x32&.io.mrpowers.tsumugi.proto.RatioOfSumsH\x00R\x0bratioOfSums\x12\x35\n\x04size\x18\x18 \x01(\x0b\x32\x1f.io.mrpowers.tsumugi.proto.SizeH\x00R\x04size\x12]\n\x12standard_deviation\x18\x19 \x01(\x0b\x32,.io.mrpowers.tsumugi.proto.StandardDeviationH\x00R\x11standardDeviation\x12\x32\n\x03sum\x18\x1a \x01(\x0b\x32\x1e.io.mrpowers.tsumugi.proto.SumH\x00R\x03sum\x12[\n\x12unique_value_ratio\x18\x1b \x01(\x0b\x32+.io.mrpowers.tsumugi.proto.UniqueValueRatioH\x00R\x10uniqueValueRatio\x12G\n\nuniqueness\x18\x1c \x01(\x0b\x32%.io.mrpowers.tsumugi.proto.UniquenessH\x00R\nuniquenessB\n\n\x08\x61nalyzer"\xc5\x02\n\x0f\x41nalyzerOptions\x12_\n\x0enull_behaviour\x18\x01 \x01(\x0e\x32\x38.io.mrpowers.tsumugi.proto.AnalyzerOptions.NullBehaviourR\rnullBehaviour\x12o\n\x14\x66iltered_row_outcome\x18\x02 \x01(\x0e\x32=.io.mrpowers.tsumugi.proto.AnalyzerOptions.FilteredRowOutcomeR\x12\x66ilteredRowOutcome"6\n\rNullBehaviour\x12\n\n\x06Ignore\x10\x00\x12\x0f\n\x0b\x45mptyString\x10\x01\x12\x08\n\x04\x46\x61il\x10\x02"(\n\x12\x46ilteredRowOutcome\x12\x08\n\x04NULL\x10\x00\x12\x08\n\x04TRUE\x10\x01"R\n\x13\x41pproxCountDistinct\x12\x16\n\x06\x63olumn\x18\x01 \x01(\tR\x06\x63olumn\x12\x19\n\x05where\x18\x02 \x01(\tH\x00R\x05where\x88\x01\x01\x42\x08\n\x06_where"\xa8\x01\n\x0e\x41pproxQuantile\x12\x16\n\x06\x63olumn\x18\x01 \x01(\tR\x06\x63olumn\x12\x1a\n\x08quantile\x18\x02 \x01(\x01R\x08quantile\x12*\n\x0erelative_error\x18\x03 \x01(\x01H\x00R\rrelativeError\x88\x01\x01\x12\x19\n\x05where\x18\x04 \x01(\tH\x01R\x05where\x88\x01\x01\x42\x11\n\x0f_relative_errorB\x08\n\x06_where"\x86\x01\n\x0f\x41pproxQuantiles\x12\x16\n\x06\x63olumn\x18\x01 \x01(\tR\x06\x63olumn\x12\x1c\n\tquantiles\x18\x02 \x03(\x01R\tquantiles\x12*\n\x0erelative_error\x18\x03 \x01(\x01H\x00R\rrelativeError\x88\x01\x01\x42\x11\n\x0f_relative_error"\r\n\x0b\x43olumnCount"\x91\x01\n\x0c\x43ompleteness\x12\x16\n\x06\x63olumn\x18\x01 \x01(\tR\x06\x63olumn\x12\x19\n\x05where\x18\x02 \x01(\tH\x00R\x05where\x88\x01\x01\x12\x44\n\x07options\x18\x03 \x01(\x0b\x32*.io.mrpowers.tsumugi.proto.AnalyzerOptionsR\x07optionsB\x08\n\x06_where"\xdc\x01\n\nCompliance\x12\x1a\n\x08instance\x18\x01 \x01(\tR\x08instance\x12\x1c\n\tpredicate\x18\x02 \x01(\tR\tpredicate\x12\x19\n\x05where\x18\x03 \x01(\tH\x00R\x05where\x88\x01\x01\x12\x18\n\x07\x63olumns\x18\x04 \x03(\tR\x07\x63olumns\x12I\n\x07options\x18\x05 \x01(\x0b\x32*.io.mrpowers.tsumugi.proto.AnalyzerOptionsH\x01R\x07options\x88\x01\x01\x42\x08\n\x06_whereB\n\n\x08_options"z\n\x0b\x43orrelation\x12!\n\x0c\x66irst_column\x18\x01 \x01(\tR\x0b\x66irstColumn\x12#\n\rsecond_column\x18\x02 \x01(\tR\x0csecondColumn\x12\x19\n\x05where\x18\x03 \x01(\tH\x00R\x05where\x88\x01\x01\x42\x08\n\x06_where")\n\rCountDistinct\x12\x18\n\x07\x63olumns\x18\x01 \x03(\tR\x07\x63olumns"-\n\tCustomSql\x12 \n\x0b\x65xpressions\x18\x01 \x01(\tR\x0b\x65xpressions"G\n\x08\x44\x61taType\x12\x16\n\x06\x63olumn\x18\x01 \x01(\tR\x06\x63olumn\x12\x19\n\x05where\x18\x02 \x01(\tH\x00R\x05where\x88\x01\x01\x42\x08\n\x06_where"M\n\x0c\x44istinctness\x12\x18\n\x07\x63olumns\x18\x01 \x03(\tR\x07\x63olumns\x12\x19\n\x05where\x18\x02 \x01(\tH\x00R\x05where\x88\x01\x01\x42\x08\n\x06_where"F\n\x07\x45ntropy\x12\x16\n\x06\x63olumn\x18\x01 \x01(\tR\x06\x63olumn\x12\x19\n\x05where\x18\x02 \x01(\tH\x00R\x05where\x88\x01\x01\x42\x08\n\x06_where"h\n\rExactQuantile\x12\x16\n\x06\x63olumn\x18\x01 \x01(\tR\x06\x63olumn\x12\x1a\n\x08quantile\x18\x02 \x01(\x01R\x08quantile\x12\x19\n\x05where\x18\x03 \x01(\tH\x00R\x05where\x88\x01\x01\x42\x08\n\x06_where"\x9a\x05\n\tHistogram\x12\x16\n\x06\x63olumn\x18\x01 \x01(\tR\x06\x63olumn\x12+\n\x0fmax_detail_bins\x18\x02 \x01(\x05H\x00R\rmaxDetailBins\x88\x01\x01\x12\x19\n\x05where\x18\x03 \x01(\tH\x01R\x05where\x88\x01\x01\x12\x44\n\x1c\x63ompute_frequencies_as_ratio\x18\x04 \x01(\x08H\x02R\x19\x63omputeFrequenciesAsRatio\x88\x01\x01\x12j\n\x12\x61ggregate_function\x18\x05 \x01(\x0b\x32\x36.io.mrpowers.tsumugi.proto.Histogram.AggregateFunctionH\x03R\x11\x61ggregateFunction\x88\x01\x01\x1a\xa4\x02\n\x11\x41ggregateFunction\x12g\n\x0f\x63ount_aggregate\x18\x01 \x01(\x0b\x32<.io.mrpowers.tsumugi.proto.Histogram.AggregateFunction.CountH\x00R\x0e\x63ountAggregate\x12\x61\n\rsum_aggregate\x18\x02 \x01(\x0b\x32:.io.mrpowers.tsumugi.proto.Histogram.AggregateFunction.SumH\x00R\x0csumAggregate\x1a\x07\n\x05\x43ount\x1a$\n\x03Sum\x12\x1d\n\nagg_column\x18\x01 \x01(\tR\taggColumnB\x14\n\x12\x61ggregate_functionB\x12\n\x10_max_detail_binsB\x08\n\x06_whereB\x1f\n\x1d_compute_frequencies_as_ratioB\x15\n\x13_aggregate_function"\xa0\x02\n\tKLLSketch\x12\x16\n\x06\x63olumn\x18\x01 \x01(\tR\x06\x63olumn\x12^\n\x0ekll_parameters\x18\x02 \x01(\x0b\x32\x32.io.mrpowers.tsumugi.proto.KLLSketch.KLLParametersH\x00R\rkllParameters\x88\x01\x01\x1a\x87\x01\n\rKLLParameters\x12\x1f\n\x0bsketch_size\x18\x01 \x01(\x05R\nsketchSize\x12)\n\x10shrinking_factor\x18\x02 \x01(\x01R\x0fshrinkingFactor\x12*\n\x11number_of_buckets\x18\x03 \x01(\x05R\x0fnumberOfBucketsB\x11\n\x0f_kll_parameters"\x9f\x01\n\tMaxLength\x12\x16\n\x06\x63olumn\x18\x01 \x01(\tR\x06\x63olumn\x12\x19\n\x05where\x18\x02 \x01(\tH\x00R\x05where\x88\x01\x01\x12I\n\x07options\x18\x03 \x01(\x0b\x32*.io.mrpowers.tsumugi.proto.AnalyzerOptionsH\x01R\x07options\x88\x01\x01\x42\x08\n\x06_whereB\n\n\x08_options"\x9d\x01\n\x07Maximum\x12\x16\n\x06\x63olumn\x18\x01 \x01(\tR\x06\x63olumn\x12\x19\n\x05where\x18\x02 \x01(\tH\x00R\x05where\x88\x01\x01\x12I\n\x07options\x18\x03 \x01(\x0b\x32*.io.mrpowers.tsumugi.proto.AnalyzerOptionsH\x01R\x07options\x88\x01\x01\x42\x08\n\x06_whereB\n\n\x08_options"C\n\x04Mean\x12\x16\n\x06\x63olumn\x18\x01 \x01(\tR\x06\x63olumn\x12\x19\n\x05where\x18\x02 \x01(\tH\x00R\x05where\x88\x01\x01\x42\x08\n\x06_where"\x9f\x01\n\tMinLength\x12\x16\n\x06\x63olumn\x18\x01 \x01(\tR\x06\x63olumn\x12\x19\n\x05where\x18\x02 \x01(\tH\x00R\x05where\x88\x01\x01\x12I\n\x07options\x18\x03 \x01(\x0b\x32*.io.mrpowers.tsumugi.proto.AnalyzerOptionsH\x01R\x07options\x88\x01\x01\x42\x08\n\x06_whereB\n\n\x08_options"\x9d\x01\n\x07Minimum\x12\x16\n\x06\x63olumn\x18\x01 \x01(\tR\x06\x63olumn\x12\x19\n\x05where\x18\x02 \x01(\tH\x00R\x05where\x88\x01\x01\x12I\n\x07options\x18\x03 \x01(\x0b\x32*.io.mrpowers.tsumugi.proto.AnalyzerOptionsH\x01R\x07options\x88\x01\x01\x42\x08\n\x06_whereB\n\n\x08_options"R\n\x11MutualInformation\x12\x18\n\x07\x63olumns\x18\x01 \x03(\tR\x07\x63olumns\x12\x19\n\x05where\x18\x02 \x01(\tH\x00R\x05where\x88\x01\x01\x42\x08\n\x06_where"\xbc\x01\n\x0cPatternMatch\x12\x16\n\x06\x63olumn\x18\x01 \x01(\tR\x06\x63olumn\x12\x18\n\x07pattern\x18\x02 \x01(\tR\x07pattern\x12\x19\n\x05where\x18\x03 \x01(\tH\x00R\x05where\x88\x01\x01\x12I\n\x07options\x18\x04 \x01(\x0b\x32*.io.mrpowers.tsumugi.proto.AnalyzerOptionsH\x01R\x07options\x88\x01\x01\x42\x08\n\x06_whereB\n\n\x08_options"r\n\x0bRatioOfSums\x12\x1c\n\tnumerator\x18\x01 \x01(\tR\tnumerator\x12 \n\x0b\x64\x65nominator\x18\x02 \x01(\tR\x0b\x64\x65nominator\x12\x19\n\x05where\x18\x03 \x01(\tH\x00R\x05where\x88\x01\x01\x42\x08\n\x06_where"+\n\x04Size\x12\x19\n\x05where\x18\x01 \x01(\tH\x00R\x05where\x88\x01\x01\x42\x08\n\x06_where"P\n\x11StandardDeviation\x12\x16\n\x06\x63olumn\x18\x01 \x01(\tR\x06\x63olumn\x12\x19\n\x05where\x18\x02 \x01(\tH\x00R\x05where\x88\x01\x01\x42\x08\n\x06_where"B\n\x03Sum\x12\x16\n\x06\x63olumn\x18\x01 \x01(\tR\x06\x63olumn\x12\x19\n\x05where\x18\x02 \x01(\tH\x00R\x05where\x88\x01\x01\x42\x08\n\x06_where"\xa8\x01\n\x10UniqueValueRatio\x12\x18\n\x07\x63olumns\x18\x01 \x03(\tR\x07\x63olumns\x12\x19\n\x05where\x18\x02 \x01(\tH\x00R\x05where\x88\x01\x01\x12I\n\x07options\x18\x03 \x01(\x0b\x32*.io.mrpowers.tsumugi.proto.AnalyzerOptionsH\x01R\x07options\x88\x01\x01\x42\x08\n\x06_whereB\n\n\x08_options"\xa2\x01\n\nUniqueness\x12\x18\n\x07\x63olumns\x18\x01 \x03(\tR\x07\x63olumns\x12\x19\n\x05where\x18\x02 \x01(\tH\x00R\x05where\x88\x01\x01\x12I\n\x07options\x18\x03 \x01(\x0b\x32*.io.mrpowers.tsumugi.proto.AnalyzerOptionsH\x01R\x07options\x88\x01\x01\x42\x08\n\x06_whereB\n\n\x08_optionsB\xc9\x01\n\x1d\x63om.io.mrpowers.tsumugi.protoB\x0e\x41nalyzersProtoP\x01Z\rtsumugi/proto\xa0\x01\x01\xa2\x02\x04IMTP\xaa\x02\x19Io.Mrpowers.Tsumugi.Proto\xca\x02\x19Io\\Mrpowers\\Tsumugi\\Proto\xe2\x02%Io\\Mrpowers\\Tsumugi\\Proto\\GPBMetadata\xea\x02\x1cIo::Mrpowers::Tsumugi::Protob\x06proto3'
)

_globals = globals()
_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, _globals)
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, "analyzers_pb2", _globals)
if not _descriptor._USE_C_DESCRIPTORS:
    _globals["DESCRIPTOR"]._loaded_options = None
    _globals[
        "DESCRIPTOR"
    ]._serialized_options = b"\n\035com.io.mrpowers.tsumugi.protoB\016AnalyzersProtoP\001Z\rtsumugi/proto\240\001\001\242\002\004IMTP\252\002\031Io.Mrpowers.Tsumugi.Proto\312\002\031Io\\Mrpowers\\Tsumugi\\Proto\342\002%Io\\Mrpowers\\Tsumugi\\Proto\\GPBMetadata\352\002\034Io::Mrpowers::Tsumugi::Proto"
    _globals["_ANALYZER"]._serialized_start = 47
    _globals["_ANALYZER"]._serialized_end = 2186
    _globals["_ANALYZEROPTIONS"]._serialized_start = 2189
    _globals["_ANALYZEROPTIONS"]._serialized_end = 2514
    _globals["_ANALYZEROPTIONS_NULLBEHAVIOUR"]._serialized_start = 2418
    _globals["_ANALYZEROPTIONS_NULLBEHAVIOUR"]._serialized_end = 2472
    _globals["_ANALYZEROPTIONS_FILTEREDROWOUTCOME"]._serialized_start = 2474
    _globals["_ANALYZEROPTIONS_FILTEREDROWOUTCOME"]._serialized_end = 2514
    _globals["_APPROXCOUNTDISTINCT"]._serialized_start = 2516
    _globals["_APPROXCOUNTDISTINCT"]._serialized_end = 2598
    _globals["_APPROXQUANTILE"]._serialized_start = 2601
    _globals["_APPROXQUANTILE"]._serialized_end = 2769
    _globals["_APPROXQUANTILES"]._serialized_start = 2772
    _globals["_APPROXQUANTILES"]._serialized_end = 2906
    _globals["_COLUMNCOUNT"]._serialized_start = 2908
    _globals["_COLUMNCOUNT"]._serialized_end = 2921
    _globals["_COMPLETENESS"]._serialized_start = 2924
    _globals["_COMPLETENESS"]._serialized_end = 3069
    _globals["_COMPLIANCE"]._serialized_start = 3072
    _globals["_COMPLIANCE"]._serialized_end = 3292
    _globals["_CORRELATION"]._serialized_start = 3294
    _globals["_CORRELATION"]._serialized_end = 3416
    _globals["_COUNTDISTINCT"]._serialized_start = 3418
    _globals["_COUNTDISTINCT"]._serialized_end = 3459
    _globals["_CUSTOMSQL"]._serialized_start = 3461
    _globals["_CUSTOMSQL"]._serialized_end = 3506
    _globals["_DATATYPE"]._serialized_start = 3508
    _globals["_DATATYPE"]._serialized_end = 3579
    _globals["_DISTINCTNESS"]._serialized_start = 3581
    _globals["_DISTINCTNESS"]._serialized_end = 3658
    _globals["_ENTROPY"]._serialized_start = 3660
    _globals["_ENTROPY"]._serialized_end = 3730
    _globals["_EXACTQUANTILE"]._serialized_start = 3732
    _globals["_EXACTQUANTILE"]._serialized_end = 3836
    _globals["_HISTOGRAM"]._serialized_start = 3839
    _globals["_HISTOGRAM"]._serialized_end = 4505
    _globals["_HISTOGRAM_AGGREGATEFUNCTION"]._serialized_start = 4127
    _globals["_HISTOGRAM_AGGREGATEFUNCTION"]._serialized_end = 4419
    _globals["_HISTOGRAM_AGGREGATEFUNCTION_COUNT"]._serialized_start = 4352
    _globals["_HISTOGRAM_AGGREGATEFUNCTION_COUNT"]._serialized_end = 4359
    _globals["_HISTOGRAM_AGGREGATEFUNCTION_SUM"]._serialized_start = 4361
    _globals["_HISTOGRAM_AGGREGATEFUNCTION_SUM"]._serialized_end = 4397
    _globals["_KLLSKETCH"]._serialized_start = 4508
    _globals["_KLLSKETCH"]._serialized_end = 4796
    _globals["_KLLSKETCH_KLLPARAMETERS"]._serialized_start = 4642
    _globals["_KLLSKETCH_KLLPARAMETERS"]._serialized_end = 4777
    _globals["_MAXLENGTH"]._serialized_start = 4799
    _globals["_MAXLENGTH"]._serialized_end = 4958
    _globals["_MAXIMUM"]._serialized_start = 4961
    _globals["_MAXIMUM"]._serialized_end = 5118
    _globals["_MEAN"]._serialized_start = 5120
    _globals["_MEAN"]._serialized_end = 5187
    _globals["_MINLENGTH"]._serialized_start = 5190
    _globals["_MINLENGTH"]._serialized_end = 5349
    _globals["_MINIMUM"]._serialized_start = 5352
    _globals["_MINIMUM"]._serialized_end = 5509
    _globals["_MUTUALINFORMATION"]._serialized_start = 5511
    _globals["_MUTUALINFORMATION"]._serialized_end = 5593
    _globals["_PATTERNMATCH"]._serialized_start = 5596
    _globals["_PATTERNMATCH"]._serialized_end = 5784
    _globals["_RATIOOFSUMS"]._serialized_start = 5786
    _globals["_RATIOOFSUMS"]._serialized_end = 5900
    _globals["_SIZE"]._serialized_start = 5902
    _globals["_SIZE"]._serialized_end = 5945
    _globals["_STANDARDDEVIATION"]._serialized_start = 5947
    _globals["_STANDARDDEVIATION"]._serialized_end = 6027
    _globals["_SUM"]._serialized_start = 6029
    _globals["_SUM"]._serialized_end = 6095
    _globals["_UNIQUEVALUERATIO"]._serialized_start = 6098
    _globals["_UNIQUEVALUERATIO"]._serialized_end = 6266
    _globals["_UNIQUENESS"]._serialized_start = 6269
    _globals["_UNIQUENESS"]._serialized_end = 6431
# @@protoc_insertion_point(module_scope)
