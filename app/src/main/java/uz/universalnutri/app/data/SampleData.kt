package uz.universalnutri.app.data

/** Catalogue inserted on the first start (see ShopRepository.seedIfEmpty). Prices in so'm. */
object SampleData {
    private const val MUSCLE = "Muscle gain"
    private const val LOSS = "Weight loss"
    private const val ENDURANCE = "Endurance"
    private const val HEALTH = "Health"

    /** Training goals a product can belong to (used by the admin form). */
    val goals = listOf(MUSCLE, LOSS, ENDURANCE, HEALTH)

    /** Demo administrator for the admin screen. Change it before a real release! */
    const val ADMIN_EMAIL = "admin@universalnutri.uz"
    const val ADMIN_PASSWORD = "admin2026"

    val products = listOf(
        Product(
            name = "Gold Standard 100% Whey", brand = "Optimum Nutrition", category = "Protein", goal = MUSCLE,
            description = "The world's best-selling whey. 24 g of protein and 5.5 g of BCAA per scoop, mixes in a shaker in seconds. Take one scoop after training or between meals.",
            price = 1_150_000, oldPrice = 1_290_000, stock = 18, weight = "2.27 kg", servings = 74,
            proteinPerServing = 24, flavor = "Double Rich Chocolate", rating = 4.9, reviews = 412, emoji = "🥛"
        ),
        Product(
            name = "Isolate Pro Zero", brand = "UniversalNutri Lab", category = "Protein", goal = LOSS,
            description = "Pure whey isolate: 27 g protein, 0 g sugar, under 1 g fat. Ideal while cutting or for people with mild lactose sensitivity.",
            price = 890_000, stock = 22, weight = "1.8 kg", servings = 60,
            proteinPerServing = 27, flavor = "Vanilla Ice Cream", rating = 4.7, reviews = 158, emoji = "🥤"
        ),
        Product(
            name = "Casein Night Recovery", brand = "Dymatize", category = "Protein", goal = MUSCLE,
            description = "Slow micellar casein releases amino acids for up to 7 hours. Drink before sleep to protect muscles overnight.",
            price = 760_000, stock = 9, weight = "1.8 kg", servings = 55,
            proteinPerServing = 25, flavor = "Cookies & Cream", rating = 4.6, reviews = 87, emoji = "🌙"
        ),
        Product(
            name = "Serious Mass Gainer", brand = "Optimum Nutrition", category = "Gainer", goal = MUSCLE,
            description = "1250 kcal and 50 g protein per serving for hard-gainers. Mix with milk to get even more calories.",
            price = 820_000, oldPrice = 950_000, stock = 12, weight = "5.44 kg", servings = 16,
            proteinPerServing = 50, flavor = "Banana", rating = 4.5, reviews = 203, emoji = "🍌"
        ),
        Product(
            name = "Creatine Monohydrate", brand = "MyProtein", category = "Creatine", goal = MUSCLE,
            description = "100% micronised creatine monohydrate. 5 g a day increases strength and power in short, intense workouts.",
            price = 290_000, stock = 40, weight = "500 g", servings = 100,
            flavor = "Unflavoured", rating = 4.8, reviews = 356, emoji = "🧪"
        ),
        Product(
            name = "Creapure Caps", brand = "Scitec Nutrition", category = "Creatine", goal = ENDURANCE,
            description = "German Creapure® creatine in easy capsules – no shaker needed. 4 capsules = 3 g creatine.",
            price = 240_000, stock = 25, weight = "120 caps", servings = 30,
            flavor = "—", rating = 4.4, reviews = 64, emoji = "💊"
        ),
        Product(
            name = "C4 Original Pre-Workout", brand = "Cellucor", category = "Pre-workout", goal = ENDURANCE,
            description = "150 mg caffeine, beta-alanine and creatine nitrate for explosive energy and focus. Take 20–30 minutes before training.",
            price = 380_000, oldPrice = 430_000, stock = 15, weight = "390 g", servings = 60,
            flavor = "Fruit Punch", rating = 4.6, reviews = 241, emoji = "⚡"
        ),
        Product(
            name = "Pump Zero Stim-Free", brand = "UniversalNutri Lab", category = "Pre-workout", goal = HEALTH,
            description = "Caffeine-free pre-workout with L-citrulline and beetroot for a great pump at evening workouts. Won't ruin your sleep.",
            price = 310_000, stock = 0, weight = "300 g", servings = 30,
            flavor = "Sour Apple", rating = 4.3, reviews = 39, emoji = "🍏"
        ),
        Product(
            name = "BCAA 2:1:1 Powder", brand = "Scitec Nutrition", category = "Amino acids", goal = ENDURANCE,
            description = "Leucine, isoleucine and valine in the classic 2:1:1 ratio. Sip during long workouts to fight fatigue.",
            price = 270_000, stock = 30, weight = "400 g", servings = 40,
            flavor = "Watermelon", rating = 4.5, reviews = 118, emoji = "🍉"
        ),
        Product(
            name = "L-Glutamine", brand = "Optimum Nutrition", category = "Amino acids", goal = HEALTH,
            description = "5 g of pure L-glutamine per serving to support recovery and the immune system after hard training.",
            price = 220_000, stock = 20, weight = "300 g", servings = 60,
            flavor = "Unflavoured", rating = 4.2, reviews = 51, emoji = "🔬"
        ),
        Product(
            name = "Opti-Men Multivitamin", brand = "Optimum Nutrition", category = "Vitamins", goal = HEALTH,
            description = "75+ vitamins, minerals and plant extracts made for active men. 3 tablets a day with food.",
            price = 330_000, stock = 27, weight = "150 tabs", servings = 50,
            flavor = "—", rating = 4.7, reviews = 190, emoji = "🌿"
        ),
        Product(
            name = "Omega-3 Fish Oil", brand = "Now Foods", category = "Vitamins", goal = HEALTH,
            description = "1000 mg fish oil with 180 mg EPA and 120 mg DHA for heart, joints and brain.",
            price = 180_000, oldPrice = 210_000, stock = 50, weight = "200 softgels", servings = 100,
            flavor = "—", rating = 4.8, reviews = 274, emoji = "🐟"
        ),
        Product(
            name = "Vitamin D3 + K2", brand = "UniversalNutri Lab", category = "Vitamins", goal = HEALTH,
            description = "2000 IU D3 with K2 (MK-7) – important in winter when there is little sun. One capsule a day.",
            price = 120_000, stock = 35, weight = "90 caps", servings = 90,
            flavor = "—", rating = 4.6, reviews = 72, emoji = "☀️"
        ),
        Product(
            name = "Protein Bar Crunch (12 pcs)", brand = "Quest", category = "Bars", goal = LOSS,
            description = "20 g protein and only 1 g sugar in a crunchy bar. A box of 12 – a healthy snack for every working day.",
            price = 360_000, stock = 14, weight = "12 × 60 g", servings = 12,
            proteinPerServing = 20, flavor = "Chocolate Chip Cookie Dough", rating = 4.7, reviews = 133, emoji = "🍫"
        ),
        Product(
            name = "Hydro Burn L-Carnitine", brand = "Scitec Nutrition", category = "Fat burners", goal = LOSS,
            description = "Liquid L-carnitine with green tea extract. Helps turn fat into energy during cardio.",
            price = 210_000, stock = 16, weight = "1000 ml", servings = 50,
            flavor = "Lemon & Lime", rating = 4.1, reviews = 58, emoji = "🔥"
        ),
        Product(
            name = "Isotonic Electrolytes", brand = "UniversalNutri Lab", category = "Amino acids", goal = ENDURANCE,
            description = "Sodium, potassium and magnesium for runners and cyclists. Prevents cramps on hot Tashkent days.",
            price = 150_000, stock = 45, weight = "500 g", servings = 25,
            flavor = "Orange", rating = 4.4, reviews = 47, emoji = "💧"
        )
    )
}
