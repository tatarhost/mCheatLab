using Microsoft.Web.WebView2.Core;
using Microsoft.Web.WebView2.WinForms;

namespace WorkInkBrowser;

internal sealed class BrowserForm : Form
{
    private readonly WebView2 browser = new();
    private readonly ToolStrip toolbar = new();
    private readonly ToolStripButton backButton = new("←");
    private readonly ToolStripButton forwardButton = new("→");
    private readonly ToolStripButton reloadButton = new("⟳");
    private readonly ToolStripTextBox addressBar = new();

    public BrowserForm()
    {
        Text = "WorkInk Browser";
        StartPosition = FormStartPosition.CenterScreen;
        MinimumSize = new Size(760, 520);
        Size = new Size(1280, 820);
        Font = new Font("Segoe UI", 9F);

        toolbar.Dock = DockStyle.Top;
        toolbar.GripStyle = ToolStripGripStyle.Hidden;
        toolbar.RenderMode = ToolStripRenderMode.System;
        toolbar.Padding = new Padding(6, 5, 6, 5);
        toolbar.Stretch = true;

        backButton.ToolTipText = "Назад";
        backButton.Enabled = false;
        backButton.Click += (_, _) =>
        {
            if (browser.CoreWebView2?.CanGoBack == true)
                browser.CoreWebView2.GoBack();
        };

        forwardButton.ToolTipText = "Вперёд";
        forwardButton.Enabled = false;
        forwardButton.Click += (_, _) =>
        {
            if (browser.CoreWebView2?.CanGoForward == true)
                browser.CoreWebView2.GoForward();
        };

        reloadButton.ToolTipText = "Обновить";
        reloadButton.Click += (_, _) => browser.CoreWebView2?.Reload();

        addressBar.AutoSize = false;
        addressBar.Size = new Size(900, 26);
        addressBar.ToolTipText = "Введите адрес сайта";
        addressBar.KeyDown += (_, e) =>
        {
            if (e.KeyCode == Keys.Enter)
            {
                e.SuppressKeyPress = true;
                NavigateFromAddressBar();
            }
        };

        toolbar.Items.Add(backButton);
        toolbar.Items.Add(forwardButton);
        toolbar.Items.Add(reloadButton);
        toolbar.Items.Add(new ToolStripSeparator());
        toolbar.Items.Add(addressBar);

        browser.Dock = DockStyle.Fill;
        browser.DefaultBackgroundColor = Color.White;

        Controls.Add(browser);
        Controls.Add(toolbar);

        Load += BrowserForm_Load;
        FormClosed += (_, _) => browser.Dispose();
    }

    private async void BrowserForm_Load(object? sender, EventArgs e)
    {
        try
        {
            string userDataFolder = Path.Combine(
                Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
                "WorkInkBrowser",
                "WebView2");

            Directory.CreateDirectory(userDataFolder);

            CoreWebView2Environment environment =
                await CoreWebView2Environment.CreateAsync(userDataFolder: userDataFolder);

            await browser.EnsureCoreWebView2Async(environment);

            CoreWebView2 core = browser.CoreWebView2;

            // Use the normal WebView2 browser engine: no extensions, ad filters,
            // request interception, custom user-agent, or injected page scripts.
            core.Settings.IsScriptEnabled = true;
            core.Settings.AreDefaultScriptDialogsEnabled = true;
            core.Settings.AreDefaultContextMenusEnabled = true;
            core.Settings.IsStatusBarEnabled = true;
            core.Settings.IsZoomControlEnabled = true;

            core.NavigationStarting += (_, args) =>
            {
                if (Uri.TryCreate(args.Uri, UriKind.Absolute, out Uri? uri))
                    SetAddress(uri.AbsoluteUri);
            };

            core.SourceChanged += (_, _) =>
            {
                if (core.Source != null)
                    SetAddress(core.Source);
            };

            core.DocumentTitleChanged += (_, _) =>
            {
                string title = core.DocumentTitle;
                Text = string.IsNullOrWhiteSpace(title)
                    ? "WorkInk Browser"
                    : $"{title} — WorkInk Browser";
            };

            core.HistoryChanged += (_, _) => UpdateNavigationButtons();

            SetAddress("https://work.ink/");
            core.Navigate("https://work.ink/");
        }
        catch (Exception ex)
        {
            MessageBox.Show(
                "Не удалось запустить браузерный движок WebView2.\n\n" +
                "Установи Microsoft Edge WebView2 Runtime и запусти браузер снова.\n\n" +
                ex.Message,
                "WorkInk Browser",
                MessageBoxButtons.OK,
                MessageBoxIcon.Error);
        }
    }

    private void NavigateFromAddressBar()
    {
        if (browser.CoreWebView2 == null)
            return;

        string input = addressBar.Text.Trim();
        if (input.Length == 0)
            return;

        string target;
        if (Uri.TryCreate(input, UriKind.Absolute, out Uri? absoluteUri) &&
            (absoluteUri.Scheme == Uri.UriSchemeHttp ||
             absoluteUri.Scheme == Uri.UriSchemeHttps))
        {
            target = absoluteUri.AbsoluteUri;
        }
        else if (!input.Contains(' ') && input.Contains('.'))
        {
            target = "https://" + input;
        }
        else
        {
            target = "https://www.google.com/search?q=" + Uri.EscapeDataString(input);
        }

        browser.CoreWebView2.Navigate(target);
    }

    private void SetAddress(string value)
    {
        if (!addressBar.Focused)
            addressBar.Text = value;
    }

    private void UpdateNavigationButtons()
    {
        CoreWebView2? core = browser.CoreWebView2;
        backButton.Enabled = core?.CanGoBack == true;
        forwardButton.Enabled = core?.CanGoForward == true;
    }
}
