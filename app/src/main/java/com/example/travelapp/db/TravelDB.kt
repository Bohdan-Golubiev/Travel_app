package com.example.travelapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.travelapp.data.dao.BookingDao
import com.example.travelapp.data.dao.DeletedBookingDao
import com.example.travelapp.data.dao.DeletedHotelDao
import com.example.travelapp.data.dao.DeletedRouteDao
import com.example.travelapp.data.dao.HotelDao
import com.example.travelapp.data.dao.PlaceDao
import com.example.travelapp.data.dao.RouteDao
import com.example.travelapp.data.dao.UserDao
import com.example.travelapp.data.entity.BookingEntity
import com.example.travelapp.data.entity.DeletedBookingEntity
import com.example.travelapp.data.entity.DeletedHotelEntity
import com.example.travelapp.data.entity.DeletedRouteEntity
import com.example.travelapp.data.entity.HotelEntity
import com.example.travelapp.data.entity.PlaceEntity
import com.example.travelapp.data.entity.RouteEntity
import com.example.travelapp.data.entity.UserEntity

@Database(
    entities = [UserEntity::class,
        RouteEntity::class,
        PlaceEntity::class,
        DeletedRouteEntity::class,
        BookingEntity::class,
        DeletedBookingEntity::class,
        HotelEntity::class,
        DeletedHotelEntity::class],
    version = 1,
    exportSchema = true
)
abstract class TravelDB : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun routeDao(): RouteDao
    abstract fun placeDao(): PlaceDao
    abstract fun deletedRouteDao(): DeletedRouteDao
    abstract fun bookingDao(): BookingDao
    abstract fun deletedBookingDao(): DeletedBookingDao
    abstract fun hotelDao(): HotelDao
    abstract fun deletedHotelDao(): DeletedHotelDao

    companion object {
        @Volatile
        private var INSTANCE: TravelDB? = null

        fun getInstance(context: Context): TravelDB =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TravelDB::class.java,
                    "travel.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}