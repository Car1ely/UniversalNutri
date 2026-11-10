package uz.universalnutri.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import uz.universalnutri.app.data.CartLine
import uz.universalnutri.app.data.Product
import uz.universalnutri.app.util.Pricing
import uz.universalnutri.app.util.Security
import uz.universalnutri.app.util.Validators
import uz.universalnutri.app.util.formatSom

class BusinessRulesTest {
    private fun product(price: Long, oldPrice: Long? = null) = Product(
        name = "Test whey", brand = "X", category = "Protein", goal = "Muscle gain", description = "",
        price = price, oldPrice = oldPrice, stock = 10, weight = "1 kg", servings = 30, flavor = "—", emoji = "🥛"
    )

    @Test fun formatSom_addsSpaces() {
        assertEquals("650 000 so'm", formatSom(650_000))
        assertEquals("1 150 000 so'm", formatSom(1_150_000))
    }

    @Test fun cartTotal_isSumOfLines() {
        val lines = listOf(CartLine(product(290_000), 2), CartLine(product(120_000), 3))
        assertEquals(940_000L, lines.sumOf { it.lineTotal })
    }

    @Test fun discountPercent_fromOldPrice() {
        assertEquals(10, product(1_161_000, 1_290_000).discountPercent)
        assertEquals(0, product(500_000).discountPercent)
    }

    @Test fun email_validation() {
        assertTrue(Validators.isValidEmail("aziz@example.uz"))
        assertFalse(Validators.isValidEmail("aziz@"))
        assertFalse(Validators.isValidEmail("no spaces@mail.uz"))
    }

    @Test fun password_rules() {
        assertTrue(Validators.isStrongPassword("gym2026"))
        assertFalse(Validators.isStrongPassword("short"))
        assertFalse(Validators.isStrongPassword("nodigitshere"))
    }

    @Test fun phone_validation_uzbekNumbers() {
        assertTrue(Validators.isValidPhone("+998 90 123 45 67"))
        assertTrue(Validators.isValidPhone("998901234567"))
        assertFalse(Validators.isValidPhone("+998 90 123"))
        assertFalse(Validators.isValidPhone("+7 900 123 45 67"))
    }

    @Test fun hash_isStable_andSalted() {
        val a = Security.hashPassword("secret1", "a@x.uz")
        assertEquals(a, Security.hashPassword("secret1", "a@x.uz"))
        assertNotEquals(a, Security.hashPassword("secret1", "b@x.uz"))
        assertEquals(64, a.length)
    }

    @Test fun promo_nutri10_givesTenPercent_andFreeDelivery() {
        val bill = Pricing.bill(600_000, "nutri10")
        assertEquals("NUTRI10", bill.promoCode)
        assertEquals(60_000L, bill.discount)
        assertEquals(0L, bill.delivery)            // 540 000 >= 500 000
        assertEquals(540_000L, bill.total)
    }

    @Test fun promo_sport15_needsMinimum() {
        assertNotNull(Pricing.checkPromo("SPORT15", 900_000))
        assertNull(Pricing.checkPromo("SPORT15", 1_000_000))
        val bill = Pricing.bill(900_000, "SPORT15")   // not valid -> ignored
        assertNull(bill.promoCode)
        assertEquals(0L, bill.discount)
    }

    @Test fun smallOrder_paysDelivery() {
        val bill = Pricing.bill(180_000, null)
        assertEquals(Pricing.DELIVERY_FEE, bill.delivery)
        assertEquals(200_000L, bill.total)
    }

    @Test fun unknownPromo_isRejected() {
        assertEquals("Unknown promo code", Pricing.checkPromo("FREE100", 2_000_000))
    }
}
