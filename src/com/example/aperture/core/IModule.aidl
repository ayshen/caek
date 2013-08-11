package com.example.aperture.core;

import android.content.Intent;


interface IModule {
    List<Intent> process(in Intent data);
}
