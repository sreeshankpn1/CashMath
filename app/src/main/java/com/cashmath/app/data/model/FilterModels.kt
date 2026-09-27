package com.cashmath.app.data.model

enum class DateRangeFilter {
    ALL,
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    CUSTOM
}

enum class SortDirection {
    NEWEST_FIRST,
    OLDEST_FIRST
}

enum class EntryTypeFilter {
    ALL,
    CASH_IN_ONLY,
    CASH_OUT_ONLY
}
