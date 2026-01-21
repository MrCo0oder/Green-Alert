package com.example.greenalert.ui.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for DestinationUiCategory enum and mapping helpers
 */
class DestinationUiCategoryTest {

    @Test
    fun `fromName with valid HOME returns HOME category`() {
        val result = DestinationUiCategory.fromName("HOME")
        assertEquals(DestinationUiCategory.HOME, result)
    }

    @Test
    fun `fromName with valid WORK returns WORK category`() {
        val result = DestinationUiCategory.fromName("WORK")
        assertEquals(DestinationUiCategory.WORK, result)
    }

    @Test
    fun `fromName with valid SCHOOL returns SCHOOL category`() {
        val result = DestinationUiCategory.fromName("SCHOOL")
        assertEquals(DestinationUiCategory.SCHOOL, result)
    }

    @Test
    fun `fromName with valid OTHER returns OTHER category`() {
        val result = DestinationUiCategory.fromName("OTHER")
        assertEquals(DestinationUiCategory.OTHER, result)
    }

    @Test
    fun `fromName with invalid name returns OTHER as default`() {
        val result = DestinationUiCategory.fromName("INVALID_CATEGORY")
        assertEquals(DestinationUiCategory.OTHER, result)
    }

    @Test
    fun `fromName with empty string returns OTHER as default`() {
        val result = DestinationUiCategory.fromName("")
        assertEquals(DestinationUiCategory.OTHER, result)
    }

    @Test
    fun `fromName with lowercase name returns OTHER as default`() {
        // Names are case-sensitive, so "home" should not match "HOME"
        val result = DestinationUiCategory.fromName("home")
        assertEquals(DestinationUiCategory.OTHER, result)
    }

    @Test
    fun `toUiCategory extension with valid HOME returns HOME category`() {
        val result = "HOME".toUiCategory()
        assertEquals(DestinationUiCategory.HOME, result)
    }

    @Test
    fun `toUiCategory extension with valid RESTAURANT returns RESTAURANT category`() {
        val result = "RESTAURANT".toUiCategory()
        assertEquals(DestinationUiCategory.RESTAURANT, result)
    }

    @Test
    fun `toUiCategory extension with invalid name returns OTHER as default`() {
        val result = "UNKNOWN".toUiCategory()
        assertEquals(DestinationUiCategory.OTHER, result)
    }

    @Test
    fun `all enum values have non-empty displayName`() {
        DestinationUiCategory.values().forEach { category ->
            assert(category.displayName.isNotEmpty()) {
                "$category has empty displayName"
            }
        }
    }

    @Test
    fun `verify all expected categories exist`() {
        val expectedCategories = listOf(
            "HOME", "WORK", "SCHOOL", "SHOPPING", "GYM",
            "RESTAURANT", "HOSPITAL", "AIRPORT", "OTHER"
        )
        
        val actualCategories = DestinationUiCategory.values().map { it.name }
        
        assertEquals(expectedCategories.size, actualCategories.size)
        expectedCategories.forEach { expected ->
            assert(actualCategories.contains(expected)) {
                "Expected category $expected not found"
            }
        }
    }
}
