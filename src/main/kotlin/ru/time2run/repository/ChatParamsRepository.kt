package ru.time2run.repository

import kotlite.annotations.SqliteRepository
import kotlite.aux.Repository
import ru.time2run.model.ChatParams

@SqliteRepository
interface ChatParamsRepository : Repository<ChatParams> {

    fun save(chatParams: ChatParams)

    fun selectBy(chatId: Long): ChatParams?

}