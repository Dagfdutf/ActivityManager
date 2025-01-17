package com.sdex.activityrunner.util

import android.content.pm.ApplicationInfo
import com.sdex.activityrunner.db.cache.ApplicationModel
import com.sdex.activityrunner.db.cache.CacheRepository
import timber.log.Timber
import javax.inject.Inject

class ApplicationsLoader @Inject constructor(
    private val cacheRepository: CacheRepository,
    private val packageInfoProvider: PackageInfoProvider,
) {

    fun syncDatabase() {
        val oldList = cacheRepository.getApplications()
        val newList = getApplicationsList()

        val listToDelete = oldList.toMutableList().also { it.removeAll(newList) }
        val listToInsert = newList.toMutableList().also { it.removeAll(oldList) }
        val listToUpdate = oldList.intersect(newList.toSet()).toList()

        if (listToDelete.isNotEmpty()) {
            val count = cacheRepository.delete(listToDelete)
            Timber.d("Deleted $count records")
        }

        if (listToInsert.isNotEmpty()) {
            val ids = cacheRepository.insert(listToInsert)
            Timber.d("Inserted ${ids.size} records")
        }

        if (listToUpdate.isNotEmpty()) {
            val count = cacheRepository.update(listToUpdate)
            Timber.d("Updated $count records")
        }
    }

    private fun getApplicationsList(): List<ApplicationModel> {
        return packageInfoProvider.getInstalledPackages()
            .mapNotNull { getApplication(it) }
    }

    private fun getApplication(
        packageName: String
    ): ApplicationModel? = try {
        val packageInfo = packageInfoProvider.getPackageInfo(packageName)
        val name = packageInfoProvider.getApplicationName(packageInfo)
        val activities = packageInfo.activities ?: emptyArray()
        val applicationInfo = packageInfo.applicationInfo
        val isSystemApp = if (applicationInfo != null) {
            (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } else {
            false
        }
        val exportedActivitiesCount = activities.count { it.isEnabled && it.exported }
        ApplicationModel(
            packageName, name, activities.size, exportedActivitiesCount, isSystemApp,
            applicationInfo.enabled,
        )
    } catch (e: Exception) {
        Timber.e(e)
        null
    }
}
