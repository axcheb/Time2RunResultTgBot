# Time2RunResultTgBot

Бот на языке Kotlin. Используется для построения таблицы с результатами забегов.

# Принцип работы

Боту нужно в течении суток (можно в разное время, но обязательно в течении суток) отправить 2 csv файла с результатами забега из мобильного приложения для волонтера. В ответ бот пришлет csv файл с результатами. Имена участников берутся из локальной базы SQLite и внешнего сайта, в случае если в локальной базе не найден код участника.
