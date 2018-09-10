package com.accounts.context

import com.accounts.config.ServiceConfiguration
import com.accounts.storage.TransactionInfoDatabase


case class ResourceContext(db:TransactionInfoDatabase, sc:ServiceConfiguration)