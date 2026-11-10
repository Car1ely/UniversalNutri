# UniversalNutri ⚡

Android-магазин спортивного питания (Business Programming, 3-week project).

**Stack:** Kotlin · Jetpack Compose · Material 3 · Navigation Compose · ViewModel + StateFlow · Room (SQLite) · KSP

## Экраны (8)
Login · Register · Home (поиск, цели, категории, сетка) · Detail · Favourites · Cart (промокод, доставка, оплата) · Order success · Profile (история заказов)

## Room: 6 сущностей
`User`, `Product`, `CartItem`, `Favorite`, `Order`, `OrderItem`. Checkout — `@Transaction placeOrder()` (списание остатков, откат при нехватке).

## Бизнес-правила
- Промокоды: `NUTRI10` (−10%), `SPORT15` (−15% от 1 000 000 so'm)
- Доставка 20 000 so'm, бесплатно от 500 000 so'm
- Пароль: SHA-256 с e-mail как солью

## Запуск
Android Studio → File → Open → папка проекта → Run. Unit-тесты: `./gradlew testDebugUnitTest` (11 тестов).
