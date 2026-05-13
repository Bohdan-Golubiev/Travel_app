package com.example.travelapp.utils

import kotlin.String

data class AppStrings(
    //Auth
    val appName: String,
    val welcomeBack: String,
    val startJourney: String,
    val signIn: String,
    val signUp: String,
    val email: String,
    val password: String,
    val enterName: String,
    val createAccount: String,
    val continueWithGoogle: String,
    val forgotPassword: String,
    val resetEmailSent: String,
    val couldNotSendReset: String,
    val show: String,
    val hide: String,
    val or: String,

    //AppDestinations
    val create: String,
    val profile: String,

    //Profile
    val signOut: String,
    val myRoutes: String,
    val myBooking: String,
    val myHotels: String,
    val myReviews: String,
    val editReview: String,
    val from: String,
    val flyBy: String,
    val language: String,
    val selectLanguage: String,

    //Routes
    val delete: String,

    //RouteDetail
    val inCorrectDate: String,
    val inCorrectDateMessage: String,
    val understand: String,
    val newRouteName: String,
    val routeName: String,
    val descriptionOpt: String,
    val description: String,
    val cancel: String,
    val edit: String,
    val save: String,

    //PlaceDetail
    val visitDate: String,
    val orderInRoute: String,
    val reviews: String,
    val noReviews: String,
    val addReview: String,
    val you: String,

    //Booking
    val route: String,

    //BookingDetail
    val direction: String,
    val service: String,
    val plane: String,
    val cost: String,
    val status: String,
    val createdAt: String,
    val departure: String,
    val arrival: String,

    //Hotels
    val noHotels: String,
    val deleteHotel: String,
    val alertHotel: String,
    val fromHotel: String,
    val routeLow: String,

    //HotelDetail
    val stayPeriod: String,
    val checkIn: String,
    val checkOut: String,
    val duration: String,
    val days: String,
    val perDay: String,
    val total: String,
    val routeUp: String,

    //Reviews
    val noReviewsScreen: String,
    val locations: String,
    val hotels: String,
    val flights: String,
    val deleteReview: String,
    val alertReview: String,
    val mark: String,
    val In: String,

    //AddReview
    val rating: String,
    val comment: String,
    val writeReview: String,
    val submit: String,
    val flyFrom: String,
    val saveChange: String,

    val createRoute: String,

    val searchPlace: String,
    val writeDescription: String,
    val date: String,
    val remove: String,

    val createdSuccessful: String,
    val makeBooking: String,

    val startPlace: String,
    val endPlace: String,
    val search: String,
    val next: String,
    val selected: String,
    val searchResult: String,

    val enterCity: String,
    val saveError: String,
    val selectedHotels: String,
    val enterCityAndTap: String,
    val searchingHotels: String,
    val noHotelsResult: String,
    val costPerDay: String,
    val selectedDates: String,
    val fromHotelSearch: String,
    val toHotelSearch: String,
    val totalCost: String,
    val update: String,
    val addToTrip: String,

    val done: String,
    val servicesBooked: String,
    val savedSuccessful: String,
    val yourTransport: String,
    val yourHotels: String,
    val noServices: String,
    val to: String,
    val daysBooked: String,
    val time: String,
    val totalLow: String,

    val toLow: String,

    val weatherOn: String,
    val avgTemp: String,
    val speed: String,
)

val EnglishStrings = AppStrings(
    //Auth
    appName = "TravelApp",
    welcomeBack = "Welcome back, explorer",
    startJourney = "Start your journey",
    signIn = "Sign In",
    signUp = "Sign Up",
    email = "Email",
    password = "Password",
    enterName = "Enter your name",
    createAccount = "Create Account",
    continueWithGoogle = "Continue with Google",
    forgotPassword = "Forgot password?",
    resetEmailSent = "Reset email sent!",
    couldNotSendReset = "Could not send reset email",
    show = "Show",
    hide = "Hide",
    or = "or",

    //AppDestinations
    create = "Create",
    profile = "Profile",

    //Profile
    signOut = "Sign out",
    myRoutes =  "My routes",
    myBooking = "Booked vehicles",
    myHotels = "Booked hotels",
    myReviews = "My reviews",
    editReview = "Edit review",
    from = "From ",
    flyBy = "Fly by ",
    language = "Language",
    selectLanguage = "Select language",

    delete = "Delete",

    inCorrectDate = "Incorrect order or values for dates",
    inCorrectDateMessage = "Dates must be set for all locations. The dates of visits should be listed in ascending order.",
    understand = "Understand",
    newRouteName = "New route name",
    routeName = "Route name",
    descriptionOpt = "Description (optional)",
    description = "Description",
    cancel = "Cancel",
    edit = "Edit",
    save = "Save",

    visitDate = "Visit date: ",
    orderInRoute = "Order in route: ",
    reviews = "Reviews ",
    noReviews = "No reviews yet. Be the first!",
    addReview = "Add review",
    you = "You",

    route = "Route: ",

    direction = "Direction",
    service = "Service",
    plane = "Plane",
    cost = "Cost",
    status = "Status",
    createdAt = "Created at",
    departure = "Departure",
    arrival = "Arrival",

    noHotels = "No hotels yet",
    deleteHotel = "Delete hotel",
    alertHotel = "Are you sure you want to delete ",
    fromHotel = " from ",
    routeLow = " route?",

    stayPeriod = "Stay period",
    checkIn = "Check-in",
    checkOut = "Check-out",
    duration = "Duration",
    days =  "day(s)",
    perDay = "Per day",
    total = "Total",
    routeUp = "Route",

    noReviewsScreen = "Check-out",
    locations = "Duration",
    hotels =  "day(s)",
    flights = "Per day",
    deleteReview = "Total",
    alertReview = "Route",
    mark = "Mark: ",
    In = "in ",

    rating = "Rating",
    comment = "Comment",
    writeReview = "Write your review here...",
    submit = "Submit",
    flyFrom = "Fly from ",
    saveChange = "Save changes",

    createRoute = "Create route",

    searchPlace = "Search place",
    writeDescription = "Write description here...",
    date = "Date",
    remove = "Remove",

    createdSuccessful = "created successful",
    makeBooking = "Make booking for trip",

    startPlace = "Start place (city)",
    endPlace = "End place (city)",
    search = "Search",
    next = "Next ",
    selected = "Selected",
    searchResult = "Search results",

    enterCity = "Enter city or place",
    saveError = "Save error: ",
    selectedHotels = "Selected hotels",
    enterCityAndTap = "Enter a city and tap Search ",
    searchingHotels = "Searching hotels...",
    noHotelsResult = "No hotels found for this location.",
    costPerDay = "Cost per day: ",
    selectedDates = "Select dates",
    fromHotelSearch = "From: ",
    toHotelSearch = "To: ",
    totalCost = "Total cost: ",
    update = "Update",
    addToTrip = "Add to trip",

    done = "Done",
    servicesBooked = "Services booked!",
    savedSuccessful = "Your selections have been saved successfully.",
    yourTransport = "Your transport",
    yourHotels = "Your hotels",
    noServices = "No services selected.",
    to = "To",
    daysBooked = "Days",
    time = "Time",
    totalLow = "total",

    toLow = "to",

    weatherOn = "Weather at ",
    avgTemp = "Average temperature ",
    speed = "km/h",
)

val UkrainianStrings = AppStrings(
    appName = "Подорожі",

    welcomeBack = "З поверненням, мандрівнику",
    startJourney = "Почни свою подорож",
    signIn = "Увійти",
    signUp = "Реєстрація",
    email = "Електронна пошта",
    password = "Пароль",
    enterName = "Введіть ім'я",
    createAccount = "Створити акаунт",
    continueWithGoogle = "Продовжити з Google",
    forgotPassword = "Забули пароль?",
    resetEmailSent = "Лист для скидання надіслано!",
    couldNotSendReset = "Не вдалося надіслати лист",
    show = "Показати",
    hide = "Сховати",
    or = "або",

    create = "Створити",
    profile = "Профіль",

    signOut = "Вийти",
    myRoutes = "Мої маршрути",
    myBooking = "Заброньований транспорт",
    myHotels = "Заброньовані готелі",
    myReviews = "Мої відгуки",
    editReview = "Редагувати відгук",
    from = "Від ",
    flyBy = "Політ на ",
    language = "Мова",
    selectLanguage = "Оберіть мову",

    delete = "Видалити",

    inCorrectDate = "Некоректний порядок або значення дат",
    inCorrectDateMessage = "Для всіх локацій мають бути встановлені дати. Дати відвідування мають йти в порядку зростання.",
    understand = "Зрозуміло",
    newRouteName = "Нова назва маршруту",
    routeName = "Назва маршруту",
    descriptionOpt = "Опис (необов'язково)",
    description = "Опис",
    cancel = "Скасувати",
    edit = "Редагувати",
    save = "Зберегти",

    visitDate = "Дата відвідування: ",
    orderInRoute = "Порядок у маршруті: ",
    reviews = "Відгуки ",
    noReviews = "Відгуків ще немає. Будьте першими!",
    addReview = "Додати відгук",
    you = "Ви",

    route = "Маршрут: ",

    direction = "Напрям",
    service = "Сервіс",
    plane = "Літак",
    cost = "Вартість",
    status = "Статус",
    createdAt = "Створено",
    departure = "Виліт",
    arrival = "Прибуття",

    noHotels = "Ще немає готелів",
    deleteHotel = "Видалити готель",
    alertHotel = "Ви впевнені, що хочете видалити ",
    fromHotel = " з ",
    routeLow = " маршруту?",

    stayPeriod = "Період проживання",
    checkIn = "Дата заїзду",
    checkOut = "Дата виїзду",
    duration = "Тривалість",
    days = "дн.",
    perDay = "За день",
    total = "Всього",
    routeUp = "Маршрут",

    noReviewsScreen = "Відгуків ще немає",
    locations = "Місця",
    hotels = "Готелі",
    flights = "Рейси",
    deleteReview = "Видалити відгук?",
    alertReview = "Цю дію неможливо скасувати.",
    mark = "Оцінка: ",
    In = "о ",

    rating = "Оцінка",
    comment = "Коментар",
    writeReview = "Напишіть свій відгук...",
    submit = "Надіслати",
    flyFrom = "Виліт з ",
    saveChange = "Зберегти зміни",

    createRoute = "Створити маршрут",

    searchPlace = "Пошук місця",
    writeDescription = "Введіть опис...",
    date = "Дата",
    remove = "Видалити",

    createdSuccessful = "успішно збережено",
    makeBooking = "Створити бронювання для подорожі",

    startPlace = "Початкове місце (місто)",
    endPlace = "Кінцеве місце (місто)",
    search = "Пошук",
    next = "Далі ",
    selected = "Обрано",
    searchResult = "Результати пошуку",

    enterCity = "Введіть місто або місце",
    saveError = "Помилка збереження: ",
    selectedHotels = "Обрані готелі",
    enterCityAndTap = "Введіть місто та натисніть «Пошук»",
    searchingHotels = "Пошук готелів...",
    noHotelsResult = "Готелів для цієї локації не знайдено",
    costPerDay = "Вартість за день: ",
    selectedDates = "Оберіть дати",
    fromHotelSearch = "З: ",
    toHotelSearch = "До: ",
    totalCost = "Загальна вартість: ",
    update = "Оновити",
    addToTrip = "Додати до подорожі",

    done = "Готово",
    servicesBooked = "Послуги заброньовано!",
    savedSuccessful = "Ваш вибір успішно збережено",
    yourTransport = "Ваш транспорт",
    yourHotels = "Ваші готелі",
    noServices = "Послуги не обрано",
    to = "До",
    daysBooked = "Днів",
    time = "Час",
    totalLow = "разом",

    toLow = "до",

    weatherOn = "Погода на ",
    avgTemp = "Середня температура ",
    speed = "км/г",
)

fun AppLocale.toStrings(): AppStrings = when (this) {
    AppLocale.ENGLISH -> EnglishStrings
    AppLocale.UKRAINIAN -> UkrainianStrings
}