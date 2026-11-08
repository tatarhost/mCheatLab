# WorkInk Browser

A minimal Windows browser window for opening Work.ink links.

## What it includes

- Microsoft Edge WebView2 engine.
- Back, forward, reload, and address bar.
- JavaScript and normal site cookies.
- No browser extensions, ad-block lists, request filters, injected scripts, or custom user-agent.

The browser keeps the normal security protections of the WebView2 engine. It does not spoof browser signals or bypass Work.ink checks. Work.ink may still show a network/VPN warning independently of browser extensions.

## Run

Download and run `WorkInkBrowser.exe` on Windows 10/11 x64.

The Microsoft Edge WebView2 Runtime must be installed. It is included with Windows 11 on supported systems; on other systems install the Evergreen Runtime from Microsoft:
https://developer.microsoft.com/microsoft-edge/webview2/

The browser stores its own cookies and site data under:
`%LOCALAPPDATA%\WorkInkBrowser\WebView2`
