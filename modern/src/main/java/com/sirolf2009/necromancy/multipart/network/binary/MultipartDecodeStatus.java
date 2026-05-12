package com.sirolf2009.necromancy.multipart.network.binary;

public enum MultipartDecodeStatus {
    OK,
    UNKNOWN_SCHEMA,
    BAD_CRC,
    TRUNCATED,
    TOPOLOGY_MISMATCH,
    PART_INDEX_OOB,
    UNSUPPORTED_FLAGS,
    ILLEGAL_OPCODE,
    MISSING_CHILD_PART,
    CORRUPT_RESOURCE_LOCATION
}
