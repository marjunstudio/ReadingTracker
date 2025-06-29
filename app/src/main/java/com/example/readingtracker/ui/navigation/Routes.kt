package com.example.readingtracker.ui.navigation

object Routes {
    const val HOME = "home"
    const val STATISTICS = "statistics"
    const val BOOK_REGISTRATION = "book_registration"
    const val BOOK_REGISTRATION_METHOD = "book_registration_method"
    const val BARCODE_SCANNER = "barcode_scanner"
    const val BOOK_CONFIRMATION = "book_confirmation"
    const val PURPOSE_SETTING = "purpose_setting"
    const val BOOK_DETAIL = "book_detail/{bookId}"
    const val MEMO_ADD = "memo_add/{bookId}"
    const val ACTION_ITEM_CREATE = "action_item_create/{bookId}/{memoId}"
    
    fun bookDetail(bookId: String) = "book_detail/$bookId"
    fun memoAdd(bookId: String) = "memo_add/$bookId"
    fun actionItemCreate(bookId: String, memoId: String) = "action_item_create/$bookId/$memoId"
}