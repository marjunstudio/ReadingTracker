# API Integration Setup Guide

## Overview

The Reading Tracker app integrates with two book APIs:
- **openBD** - For Japanese books (ISBN starting with 978-4)
- **Google Books API** - For foreign books and as a fallback

## Setup Instructions

### 1. Google Books API Key

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the "Books API" from the API Library
4. Create credentials (API Key)
5. Copy `local.properties.example` to `local.properties`
6. Add your API key:
   ```
   GOOGLE_BOOKS_API_KEY=your_actual_api_key_here
   ```

### 2. openBD API

No API key required! The openBD API is free and open to use.

## Implementation Details

### API Selection Logic

The app automatically selects the appropriate API based on ISBN:
- ISBN starting with "978-4" or "9784" → openBD (Japanese books)
- All other ISBNs → Google Books API

### Rate Limiting

- **Google Books API**: 
  - 2-second interval between requests
  - Maximum 900 requests per day (with 100 request safety margin)
- **openBD**: No rate limiting

### Caching Strategy

- API responses are cached for 7 days
- Cached data is stored in Room database
- Automatic cleanup of expired cache entries

### Error Handling

The repository returns sealed class results:
- `Success` - Book found and data retrieved
- `NotFound` - No book found for the ISBN
- `RateLimitExceeded` - Too many requests in short time
- `DailyLimitExceeded` - Daily quota reached
- `NetworkError` - No network connection
- `ApiError` - Other API errors

## Usage Example

```kotlin
@HiltViewModel
class BookSearchViewModel @Inject constructor(
    private val bookSearchRepository: BookSearchRepository
) : ViewModel() {
    
    fun searchBook(isbn: String) {
        viewModelScope.launch {
            when (val result = bookSearchRepository.searchByIsbn(isbn)) {
                is BookSearchResult.Success -> {
                    // Handle success
                    val bookData = result.bookData
                }
                BookSearchResult.NotFound -> {
                    // Show not found message
                }
                BookSearchResult.NetworkError -> {
                    // Show network error
                }
                // Handle other cases...
            }
        }
    }
}
```

## Testing

For testing without API keys:
1. The openBD API works without configuration
2. For Google Books API testing, you can use the test key provided in the documentation
3. Mock responses are available in the test directory