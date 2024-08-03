/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * 
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * 
 * For more information, please refer to <http://unlicense.org/>
*/

package org.gwiz.wurmunlimited.mods;

import org.gotti.wurmunlimited.modloader.callbacks.CallbackApi;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.Versioned;
import org.gotti.wurmunlimited.modloader.interfaces.WurmClientMod;
import org.gotti.wurmunlimited.modsupport.console.ConsoleListener;
import org.gotti.wurmunlimited.modsupport.console.ModConsole;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class QuickOpen implements WurmClientMod, Initable, ConsoleListener, Versioned {

	private static final String version = "1.1";
	private boolean quickOpenActive = true;

	@CallbackApi
	public boolean isQuickOpenActive() {
		return quickOpenActive;
	}

	@Override
	public void init() {
		try {
			ClassPool hookClassPool = HookManager.getInstance().getClassPool();
			CtClass ctWurmEventHandler = hookClassPool.getCtClass("com.wurmonline.client.WurmEventHandler");
			HookManager.getInstance().addCallback(ctWurmEventHandler, "quickopen", this);
			ctWurmEventHandler.getDeclaredMethod("mousePressed").instrument(new ExprEditor() {
				public void edit(MethodCall methodCall) throws CannotCompileException {
					if (methodCall.getMethodName().equals("sendDefaultAction")) {
						methodCall.replace("{ if (this.quickopen.isQuickOpenActive()) this.world.getHud()."
								+ "sendAction(com.wurmonline.shared.constants.PlayerAction.OPEN, $1); else $_ "
								+ "= $proceed($$); }");
					}
				}
			});
		} catch (CannotCompileException | NotFoundException e) {
			e.printStackTrace();
		}
		ModConsole.addConsoleListener(this);
	}

	@Override
	public boolean handleInput(String string, Boolean aBoolean) {
		if (string == null)
			return false;
		String[] args = string.split("\\s+");
		if (!args[0].equals("quickopen"))
			return false;
		if (args.length > 1) {
			String command = args[1];
			switch (command) {
			case "on":
				quickOpenActive = true;
				System.out.println("[QuickOpen] Enabled");
				return true;
			case "off":
				quickOpenActive = false;
				System.out.println("[QuickOpen] Disabled");
				return true;
			case "toggle":
				quickOpenActive = !quickOpenActive;
				System.out.printf("[QuickOpen] %s%n", quickOpenActive ? "Enabled" : "Disabled");
				return true;
			}
		}
		System.out.println("[QuickOpen] Valid commands are: on, off, toggle");
		return true;
	}

	@Override
	public String getVersion() {
		return version;
	}
}
