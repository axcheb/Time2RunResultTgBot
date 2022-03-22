package ru.time2run.repository

import kotlite.annotations.SqliteRepository
import kotlite.annotations.Where
import kotlite.aux.Repository
import ru.time2run.model.Athlete

@SqliteRepository
interface AthleteRepository : Repository<Athlete> {

    fun save(athletes: List<Athlete>)

    fun save(athlete: Athlete)

    @Where("barcode_id in (:ids)")
    fun selectWhere(ids: List<Int>): List<Athlete>

}