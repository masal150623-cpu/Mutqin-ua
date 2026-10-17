package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.mutqin.core.security.PasswordSecurity
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
    assertEquals("Mutqin", appName)
  }

  @Test
  fun `verify password hashing and salt verification`() {
    val salt = PasswordSecurity.generateSalt()
    val hash = PasswordSecurity.hashPassword("admin123", salt)

    assertTrue(PasswordSecurity.verifyPassword("admin123", salt, hash))
    assertFalse(PasswordSecurity.verifyPassword("wrongpass", salt, hash))
  }
}
