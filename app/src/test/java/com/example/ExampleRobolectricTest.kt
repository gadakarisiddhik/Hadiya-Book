package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.HadiyaRecord
import com.example.util.DateUtils
import com.example.util.WhatsAppHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("HadiyaBook", appName)
  }

  @Test
  fun `validate Indian phone number format`() {
    assertTrue(WhatsAppHelper.isValidIndianPhoneNumber("9876543210"))
    assertTrue(WhatsAppHelper.isValidIndianPhoneNumber("+919876543210"))
    assertTrue(WhatsAppHelper.isValidIndianPhoneNumber("09876543210"))
    assertFalse(WhatsAppHelper.isValidIndianPhoneNumber("12345"))
    assertFalse(WhatsAppHelper.isValidIndianPhoneNumber("abcdefghij"))
  }

  @Test
  fun `verify currency formatting`() {
    val formatted = DateUtils.formatCurrency(500)
    assertTrue(formatted.contains("500"))
  }

  @Test
  fun `verify HadiyaRecord creation`() {
    val record = HadiyaRecord(
      name = "Mohammed Irfan",
      phone = "9876543210",
      amount = 500,
      date = "2026-09-19",
      note = "Jumma Hadiya"
    )
    assertEquals("Mohammed Irfan", record.name)
    assertEquals(500L, record.amount)
    assertEquals("2026-09-19", record.date)
  }
}

