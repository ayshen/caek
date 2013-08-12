package com.example.aperture.core;

import android.content.Intent;


/** The communication interface between a requesting context and a module.
*/
interface IModule {

    /** Query a module for relevant actions based on an input.
    @param data An Intent containing a content query (plain text, image, video,
    ...)
    @return A List of well-formed Intents, each of which should be immediately
    usable in <code>Context.startActivity()</code>, representing actions that
    are relevant to the content query in the scope of what the module
    understands. See {@link com.example.aperture.core.Module} for keys for
    extras that can be added to the individual Intents for prettier display.
    */
    List<Intent> process(in Intent data);
}
