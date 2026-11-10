# UniversalNutri ⚡

Android-магазин спортивного питания (Business Programming, 3-week project).

**Stack:** Kotlin · Jetpack Compose · Material 3 · Navigation Compose · ViewModel + StateFlow · Room (SQLite) · KSP

## Экраны (11)
Login · Register · Home (поиск, цели, категории, сетка) · Detail (отзывы, похожие товары) · Favourites · Cart (промокод, доставка, оплата) · Order success · Profile (история заказов) · Order details (статус, отмена, «купить снова») · Admin panel · Product editor

## Room: 7 сущностей
`User`, `Product`, `CartItem`, `Favorite`, `Order`, `OrderItem`, `Review`.

Транзакции (`@Transaction`):
- `placeOrder()` — оформление заказа: списание остатков, откат при нехватке;
- `cancelOrder()` — отмена заказа: товары возвращаются на склад, статус `CANCELLED`;
- `markDelivered()` — клиент подтверждает получение, статус `DELIVERED`;
- `ReviewDao.add()` — отзыв + пересчёт рейтинга товара.

## Статусы заказа
`NEW` → `DELIVERED` (кнопка «I received the order») или `NEW` → `CANCELLED` (кнопка «Cancel order», остатки возвращаются).

## Админ-панель (обязательна для пары)
Войти под демо-админом: **admin@universalnutri.uz / admin2026** (создаётся при первом запуске, см. `SampleData`).
Профиль → Admin panel → список товаров → добавить или изменить название, цену, старую цену (скидку), остаток и т. д.

## Бизнес-правила
- Промокоды: `NUTRI10` (−10%), `SPORT15` (−15% от 1 000 000 so'm)
- Доставка 20 000 so'm, бесплатно от 500 000 so'm
- Пароль: SHA-256 с e-mail как солью
- В корзину нельзя положить больше, чем есть на складе
- Один отзыв от пользователя на товар (1–5 звёзд)

## Как запустить
1. Android Studio → **File → Open** → папка `UniversalNutri`, дождаться «Gradle sync finished».
2. **Build → Make Project** — сборка без ошибок.
3. **Device Manager** → создать эмулятор (например, Pixel) или подключить телефон по USB с включённой отладкой.
4. Нажать **Run ▶**.

Unit-тесты (14 шт.): в терминале Android Studio
```bash
./gradlew testDebugUnitTest
```

> Версия БД — 2: при обновлении со старой сборки локальная база пересоздаётся (`fallbackToDestructiveMigration`).

## Как проверить
1. **Регистрация:** Register → имя, e-mail, пароль (мин. 6 символов и цифра). Неверный пароль при входе → ошибка.
2. **Каталог:** поиск по слову «whey», фильтр по цели и категории.
3. **Товар:** открыть карточку → изменить количество → Add. Больше остатка добавить нельзя.
4. **Корзина:** промокод `NUTRI10` → адрес, телефон `+998 90 123 45 67` → Place order → экран «Order placed!».
5. **Заказ:** Profile → заказ → «I received the order» → статус `DELIVERED`. Второй заказ → «Cancel order» → `CANCELLED`, остаток товара вернулся. «Buy again» → товары снова в корзине.
6. **Отзыв:** карточка товара → Write a review → рейтинг и число отзывов изменились.
7. **Админ:** выйти → войти как `admin@universalnutri.uz` / `admin2026` → Profile → Admin panel → изменить цену, добавить товар → он появился в каталоге.
8. **Сессия:** закрыть и открыть приложение — вход сохранился. Logout → экран входа.
9. **Поворот и тёмная тема:** повернуть экран в корзине — введённый адрес не пропал; включить тёмную тему в настройках телефона.
10. **База данных:** App Inspection → Database Inspector → таблица `orders` — новые строки после заказа.
