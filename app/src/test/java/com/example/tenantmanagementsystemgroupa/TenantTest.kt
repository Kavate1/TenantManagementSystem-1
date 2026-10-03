package com.example.tenantmanagementsystemgroupa

import org.junit.Assert.assertEquals
import org.junit.Test

class TenantTest {
    @Test
    fun summaryUsesTheExerciseFormat() {
        assertEquals(
            "Tenant: John Kamau\nPhone: 0712345678\nRent paid: KSh 25000",
            Tenant("John Kamau", "0712345678", "25000").summary()
        )
    }

    @Test
    fun summaryPreservesLeadingZeroAndDecimalRent() {
        assertEquals(
            "Tenant: Mary Wanjiku\nPhone: 0712345678\nRent paid: KSh 25000.50",
            Tenant("Mary Wanjiku", "0712345678", "25000.50").summary()
        )
    }
}
