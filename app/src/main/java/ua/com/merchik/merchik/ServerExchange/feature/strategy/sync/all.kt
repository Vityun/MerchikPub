package ua.com.merchik.merchik.ServerExchange.feature.strategy.sync

import ua.com.merchik.merchik.ServerExchange.TablesExchange.PlanogrammTableExchange
import ua.com.merchik.merchik.ServerExchange.feature.SyncCallable
import ua.com.merchik.merchik.ServerExchange.feature.strategy.PlanogrammSyncStrategy
import ua.com.merchik.merchik.data.synchronization.TableName


class PlanogramSync : PlanogrammSyncStrategy(TableName.PLANOGRAMM) {
    override fun getSyncFunction(planogramm: PlanogrammTableExchange): (SyncCallable) -> Unit {
        return planogramm::planogramDownload
    }
}

class PlanogramAddressSync : PlanogrammSyncStrategy(TableName.PLANOGRAMM_ADDRESS) {
    override fun getSyncFunction(planogramm: PlanogrammTableExchange): (SyncCallable) -> Unit {
        return planogramm::planogrammAddressDownload
    }
}

class PlanogramGroupSync : PlanogrammSyncStrategy(TableName.PLANOGRAMM_GROUP) {
    override fun getSyncFunction(planogramm: PlanogrammTableExchange): (SyncCallable) -> Unit {
        return planogramm::planogrammGroupDownload
    }
}

class PlanogramImagesSync : PlanogrammSyncStrategy(TableName.PLANOGRAMM_IMAGES) {
    override fun getSyncFunction(planogramm: PlanogrammTableExchange): (SyncCallable) -> Unit {
        return planogramm::planogrammImagesDownload
    }
}

class PlanogramTypeSync : PlanogrammSyncStrategy(TableName.PLANOGRAMM_TYPE) {
    override fun getSyncFunction(planogramm: PlanogrammTableExchange): (SyncCallable) -> Unit {
        return planogramm::planorgammType
    }
}

class PlanogramVisitShowcaseSync : PlanogrammSyncStrategy(TableName.PLANOGRAMM_VIZIT_SHOWCASE) {
    override fun getSyncFunction(planogramm: PlanogrammTableExchange): (SyncCallable) -> Unit {
        return planogramm::planogrammVisitShowcase
    }
}
