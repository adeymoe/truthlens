package uk.ac.tees.mad.e4615842.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imageUri: String,
    val label: String,
    val confidence: Int,
    val isAi: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)