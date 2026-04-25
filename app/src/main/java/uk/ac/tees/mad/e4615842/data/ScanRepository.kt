package uk.ac.tees.mad.e4615842.data

import kotlinx.coroutines.flow.Flow

class ScanRepository(private val scanDao: ScanDao) {

    val allScans: Flow<List<ScanEntity>> = scanDao.getAllScans()

    suspend fun insertScan(scan: ScanEntity) = scanDao.insertScan(scan)

    suspend fun deleteScan(scan: ScanEntity) = scanDao.deleteScan(scan)

    suspend fun deleteAllScans() = scanDao.deleteAllScans()
}