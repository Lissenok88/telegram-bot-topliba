## Telegram Bot - parser for [Topliba](https://topliba.com)
==========================================================

Telegram bot for searching and downloading books from the Toplib website.

### Technology stack used:
* JDK 17
* Spring Boot 2.7
* Telegram API 6.3
* Lombok
* Maven

### Project key logic:
- You can search for a book or a specific author.
- If the bot finds a match, it will provide a list of books.
- A list of 5 books is shown on one page.
- If there are more books, you can use the arrows to turn pages.
- To select the one you need, you need to click the book number in the list.
- After clicking the book number, the book's anatomy opens.
- It is also written below:
  - “Access blocked by the copyright holder” - the book cannot be downloaded;
  - “Download introductory fragment” - the book cannot be downloaded in full;
  - “Download the book in full” - the book is downloaded in full.
- Books are downloaded in fb2 format.