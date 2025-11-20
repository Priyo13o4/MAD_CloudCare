package com.example.cloudcareapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Centralized TextFieldDefaults for consistent text visibility across all devices.
 * Fixes light gray text issue on Vivo phones and dark mode issues by explicitly setting text colors.
 */
object CloudCareTextFieldDefaults {
    
    @Composable
    fun colors(
        focusedBorderColor: Color = Color(0xFF3B82F6),
        unfocusedBorderColor: Color = Color(0xFFD1D5DB),
        errorBorderColor: Color = Color(0xFFEF4444),
        isDarkTheme: Boolean = isSystemInDarkTheme()
    ): TextFieldColors {
        // Use different text colors for dark and light themes
        val textColor = if (isDarkTheme) Color(0xFFE2E8F0) else TextPrimary
        val secondaryTextColor = if (isDarkTheme) Color(0xFF94A3B8) else TextSecondary
        val placeholderColor = if (isDarkTheme) Color(0xFF64748B) else TextTertiary
        
        return OutlinedTextFieldDefaults.colors(
            // Text colors - ensure high contrast in both themes
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,  // Always use high contrast color
            disabledTextColor = secondaryTextColor,
            errorTextColor = textColor,
            
            // Border colors
            focusedBorderColor = focusedBorderColor,
            unfocusedBorderColor = if (isDarkTheme) Color(0xFF475569) else unfocusedBorderColor,
            disabledBorderColor = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE5E7EB),
            errorBorderColor = errorBorderColor,
            
            // Label colors
            focusedLabelColor = focusedBorderColor,
            unfocusedLabelColor = secondaryTextColor,
            disabledLabelColor = if (isDarkTheme) Color(0xFF475569) else TextTertiary,
            errorLabelColor = errorBorderColor,
            
            // Placeholder colors
            focusedPlaceholderColor = placeholderColor,
            unfocusedPlaceholderColor = placeholderColor,
            disabledPlaceholderColor = if (isDarkTheme) Color(0xFF475569) else Color(0xFFD1D5DB),
            errorPlaceholderColor = placeholderColor,
            
            // Cursor color
            cursorColor = focusedBorderColor,
            errorCursorColor = errorBorderColor,
            
            // Container color (background)
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF9FAFB),
            errorContainerColor = Color.Transparent
        )
    }
    
    @Composable
    fun doctorColors(
        isDarkTheme: Boolean = isSystemInDarkTheme()
    ): TextFieldColors {
        val textColor = if (isDarkTheme) Color(0xFFE2E8F0) else DoctorTextPrimary
        val secondaryTextColor = if (isDarkTheme) Color(0xFF94A3B8) else DoctorTextSecondary
        val placeholderColor = if (isDarkTheme) Color(0xFF64748B) else DoctorTextTertiary
        
        return OutlinedTextFieldDefaults.colors(
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            disabledTextColor = secondaryTextColor,
            errorTextColor = textColor,
            
            focusedBorderColor = DoctorPrimary,
            unfocusedBorderColor = if (isDarkTheme) Color(0xFF475569) else Color(0xFFD1D5DB),
            disabledBorderColor = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE5E7EB),
            errorBorderColor = Color(0xFFEF4444),
            
            focusedLabelColor = DoctorPrimary,
            unfocusedLabelColor = secondaryTextColor,
            disabledLabelColor = if (isDarkTheme) Color(0xFF475569) else DoctorTextTertiary,
            errorLabelColor = Color(0xFFEF4444),
            
            focusedPlaceholderColor = placeholderColor,
            unfocusedPlaceholderColor = placeholderColor,
            disabledPlaceholderColor = if (isDarkTheme) Color(0xFF475569) else Color(0xFFD1D5DB),
            errorPlaceholderColor = placeholderColor,
            
            cursorColor = DoctorPrimary,
            errorCursorColor = Color(0xFFEF4444),
            
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF9FAFB),
            errorContainerColor = Color.Transparent
        )
    }
    
    @Composable
    fun hospitalColors(
        isDarkTheme: Boolean = isSystemInDarkTheme()
    ): TextFieldColors {
        val textColor = if (isDarkTheme) Color(0xFFE2E8F0) else TextPrimary
        val secondaryTextColor = if (isDarkTheme) Color(0xFF94A3B8) else TextSecondary
        val placeholderColor = if (isDarkTheme) Color(0xFF64748B) else TextTertiary
        
        return OutlinedTextFieldDefaults.colors(
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            disabledTextColor = secondaryTextColor,
            errorTextColor = textColor,
            
            focusedBorderColor = Color(0xFF10B981),
            unfocusedBorderColor = if (isDarkTheme) Color(0xFF475569) else Color(0xFFD1D5DB),
            disabledBorderColor = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE5E7EB),
            errorBorderColor = Color(0xFFEF4444),
            
            focusedLabelColor = Color(0xFF10B981),
            unfocusedLabelColor = secondaryTextColor,
            disabledLabelColor = if (isDarkTheme) Color(0xFF475569) else TextTertiary,
            errorLabelColor = Color(0xFFEF4444),
            
            focusedPlaceholderColor = placeholderColor,
            unfocusedPlaceholderColor = placeholderColor,
            disabledPlaceholderColor = if (isDarkTheme) Color(0xFF475569) else Color(0xFFD1D5DB),
            errorPlaceholderColor = placeholderColor,
            
            cursorColor = Color(0xFF10B981),
            errorCursorColor = Color(0xFFEF4444),
            
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF9FAFB),
            errorContainerColor = Color.Transparent
        )
    }
}
