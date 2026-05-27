package com.example.travelapp.viewmodel.profile

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.data.dao.HotelWithRoute
import com.example.travelapp.data.entity.HotelEntity
import com.example.travelapp.data.entity.ReviewEntity
import com.example.travelapp.data.repository.FirestoreRepository
import com.example.travelapp.data.repository.HotelRepository
import com.example.travelapp.data.repository.ReviewRepository
import com.example.travelapp.db.TravelDB
import com.example.travelapp.notification.TravelAlarmManager
import com.example.travelapp.notification.removeAlarm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class HotelStatus { UPCOMING, IN_PROGRESS, COMPLETED }
class HotelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HotelRepository(
        db = TravelDB.getInstance(application),
        context = application
    )
    private val repositoryReview = ReviewRepository(TravelDB.getInstance(application), application)
    private val dao = TravelDB.getInstance(application).hotelDao()

    private val _reviews = MutableStateFlow<List<ReviewEntity>>(emptyList())
    val reviews: StateFlow<List<ReviewEntity>> = _reviews.asStateFlow()
    private val _isLoadingReviews = MutableStateFlow(false)
    val isLoadingReviews: StateFlow<Boolean> = _isLoadingReviews.asStateFlow()
    @SuppressLint("StaticFieldLeak")
    private val ctx = application.applicationContext

    fun getHotelStatus(dateFromStr: String, dateToStr: String): HotelStatus {
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).apply {
            isLenient = false
        }

        val dateFrom = try { format.parse(dateFromStr) } catch (e: Exception) { null }
        val dateTo   = try { format.parse(dateToStr)   } catch (e: Exception) { null }

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        return when {
            dateTo   != null && dateTo.before(today) -> HotelStatus.COMPLETED
            dateFrom != null && !dateFrom.after(today)
                    && (dateTo == null || !dateTo.before(today)) -> HotelStatus.IN_PROGRESS
            else -> HotelStatus.UPCOMING
        }
    }

    fun getHotelsByUser(userId: String): Flow<List<HotelWithRoute>> =
        dao.getHotelWithRoute(userId)

    fun getHotelWithRouteById(hotelId: String): Flow<HotelWithRoute?> =
        dao.getByIdWithRoute(hotelId)

    fun deleteHotel(userId: String, hotel: HotelEntity) {
        viewModelScope.launch {
            repository.deleteHotel(userId, hotel)
            cancelCheckInReminder(hotel)
        }
    }

    val avgRating: StateFlow<Double> = reviews
        .map { list ->
            if (list.isEmpty()) 0.0
            else list.sumOf { it.mark }.toDouble() / list.size
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0.0
        )

    fun loadReviews(targetId: String) {
        viewModelScope.launch {
            _isLoadingReviews.value = true
            _reviews.value = repositoryReview.getReviewByTargetId(targetId)

            runCatching {
                val remoteReviews = FirestoreRepository().getReviewsByTargetId(targetId)
                if (remoteReviews.isNotEmpty()) {
                    _reviews.value = remoteReviews
                }
            }.onFailure {
                Log.e("FIRESTORE", "Error loading reviews", it)
            }
            _isLoadingReviews.value = false
        }
    }

    fun cancelCheckInReminder(hotel: HotelEntity) {
        val alarmIdIn = (hotel.id+hotel.dateFrom).hashCode()
        TravelAlarmManager.cancel(ctx, alarmIdIn, TravelAlarmManager.ReminderType.CHECK_IN)
        removeAlarm(ctx, alarmIdIn, TravelAlarmManager.ReminderType.CHECK_IN)

        val alarmIdOut = (hotel.id+hotel.dateTo).hashCode()
        TravelAlarmManager.cancel(ctx, alarmIdOut, TravelAlarmManager.ReminderType.CHECK_OUT)
        removeAlarm(ctx, alarmIdOut, TravelAlarmManager.ReminderType.CHECK_OUT)
        Log.d("DeleteAlarm", "Видалено ${hotel.name}")
    }
}