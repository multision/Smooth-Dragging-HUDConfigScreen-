# Smooth Dragging (HUDConfigScreen)
Smooth dragging for Eric Golde's drag system.

Original drag system created by OrangeMarshall.
Modified by Eric Golde to properly work in a client.
Edited by Mason#8979 and UghItsIsaac#6037 to smoothen out the dragging.

How did we manage this?

I was up one night working on the mod dragging for four hours, with Isaac.
We had permission from Canelex to use his mods, so we were working on porting some.
We took a look at how Canelex made his drag system, he had it inside of the drawScreen method.
We moved our drag system inside of the drawScreen method, and then boom. Smooth dragging.

This can be made even smoother believe it or not. Currently, mods are using ints for positioning.
Ints are positioned by pixels. If you were to use floats instead of ints for positioning you can achieve an even smoother system.
But if you do this I would recommend creating an anchor system, otherwise positioning mods might get hard.
