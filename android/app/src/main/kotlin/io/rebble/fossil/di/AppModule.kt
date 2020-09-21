package io.rebble.fossil.di

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.Context
import dagger.Binds
import dagger.Module

@Module
abstract class AppModule {
    @Binds
    abstract fun bindContext(application: Application): Context

    @Module
    companion object {
        fun provideBluetoothAdapter(): BluetoothAdapter {
            //TODO what to do when this returns null?
            return BluetoothAdapter.getDefaultAdapter()
        }
    }
}