#!/usr/bin/env python3
"""Apply fork-only patches to the CI checkout of Simple-Commons."""
from pathlib import Path
import re
import sys

ROOT = Path("Simple-Commons")
DOMAIN_RE = re.compile(
    r"(?:https?://(?:www\.)?simplemobiletools\.com[^\s\"'<>]*)"
    r"|(?:mailto:hello@simplemobiletools\.com)"
    r"|(?:hello@simplemobiletools\.com)"
    r"|(?:www\.simplemobiletools\.com)"
    r"|(?<![\w.])simplemobiletools\.com",
    re.IGNORECASE,
)


def patch(rel: str, replacements: list[tuple[str, str]]) -> None:
    path = ROOT / rel
    text = path.read_text(encoding="utf-8")
    for old, new in replacements:
        if old not in text:
            raise SystemExit(f"patch failed in {rel}: {old[:90]!r} not found")
        text = text.replace(old, new, 1)
    path.write_text(text, encoding="utf-8")
    print(f"patched {rel}")


def scrub_simplemobiletools_com() -> None:
    """Strip leftover website/email mentions from Commons sources and strings."""
    count = 0
    for path in ROOT.rglob("*"):
        if path.suffix.lower() not in {".kt", ".xml", ".java"}:
            continue
        text = path.read_text(encoding="utf-8")
        # Package names like com.simplemobiletools.commons contain the
        # substring "simplemobiletools.com"; only rewrite actual website/email hits.
        if DOMAIN_RE.search(text) is None:
            continue
        new = DOMAIN_RE.sub("", text)
        if DOMAIN_RE.search(new):
            raise SystemExit(f"domain still present after scrub in {path}")
        path.write_text(new, encoding="utf-8")
        count += 1
        print(f"scrubbed simplemobiletools.com from {path.relative_to(ROOT)}")
    print(f"scrubbed {count} Commons files")


def main() -> None:
    if not ROOT.exists():
        raise SystemExit("Simple-Commons/ is missing")

    fake = (
        "You are using a fake version of the app. For your own safety download the original "
        "one from www.simplemobiletools.com. Thanks"
    )

    patch(
        "commons/src/main/kotlin/com/simplemobiletools/commons/activities/BaseSimpleActivity.kt",
        [
            (
                '        if (!packageName.startsWith("com.simplemobiletools.", true)) {\n'
                "            if ((0..50).random() == 10 || baseConfig.appRunCount % 100 == 0) {\n"
                f'                val label = "{fake}"\n'
                "                ConfirmationDialog(this, label, positive = R.string.ok, negative = 0) {\n"
                "                    launchViewIntent(DEVELOPER_PLAY_STORE_URL)\n"
                "                }\n"
                "            }\n"
                "        }\n",
                "",
            ),
            (
                '        if (!packageName.contains("slootelibomelpmis".reversed(), true)) {\n'
                "            if (baseConfig.appRunCount > 100) {\n"
                f'                val label = "{fake}"\n'
                "                ConfirmationDialog(this, label, positive = R.string.ok, negative = 0) {\n"
                "                    launchViewIntent(DEVELOPER_PLAY_STORE_URL)\n"
                "                }\n"
                "                return\n"
                "            }\n"
                "        }\n\n",
                "",
            ),
        ],
    )

    patch(
        "commons/src/main/kotlin/com/simplemobiletools/commons/extensions/Activity.kt",
        [
            (
                "fun Activity.checkAppSideloading(): Boolean {\n"
                "    val isSideloaded = when (baseConfig.appSideloadingStatus) {\n"
                "        SIDELOADING_TRUE -> true\n"
                "        SIDELOADING_FALSE -> false\n"
                "        else -> isAppSideloaded()\n"
                "    }\n"
                "\n"
                "    baseConfig.appSideloadingStatus = if (isSideloaded) SIDELOADING_TRUE else SIDELOADING_FALSE\n"
                "    if (isSideloaded) {\n"
                "        showSideloadingDialog()\n"
                "    }\n"
                "\n"
                "    return isSideloaded\n"
                "}\n",
                "fun Activity.checkAppSideloading(): Boolean {\n"
                "    return false\n"
                "}\n",
            ),
        ],
    )

    patch(
        "commons/src/main/kotlin/com/simplemobiletools/commons/activities/AboutActivity.kt",
        [
            (
                'launchViewIntent("https://github.com/SimpleMobileTools")',
                'launchViewIntent("https://github.com/t0ma5/Simple-Gallery")',
            ),
            (
                "showPrivacyPolicy = showExternalLinks,",
                "showPrivacyPolicy = false,",
            ),
            (
                "                    helpUsSection = {\n"
                "                        val showHelpUsSection =\n"
                "                            remember { showGoogleRelations || !showExternalLinks }\n"
                "                        HelpUsSection(\n"
                "                            onRateUsClick = {\n"
                "                                onRateUsClick(\n"
                "                                    showConfirmationAdvancedDialog = onRateUsClickAlertDialogState::show,\n"
                "                                    showRateStarsDialog = rateStarsAlertDialogState::show\n"
                "                                )\n"
                "                            },\n"
                "                            onInviteClick = ::onInviteClick,\n"
                "                            onContributorsClick = ::onContributorsClick,\n"
                "                            showDonate = resources.getBoolean(R.bool.show_donate_in_about) && showExternalLinks,\n"
                "                            onDonateClick = ::onDonateClick,\n"
                "                            showInvite = showHelpUsSection,\n"
                "                            showRateUs = showHelpUsSection\n"
                "                        )\n"
                "                    },\n",
                "                    helpUsSection = {},\n",
            ),
            (
                "                    aboutSection = {\n"
                "                        val setupFAQ = rememberFAQ()\n"
                "                        if (!showExternalLinks || setupFAQ) {\n"
                "                            AboutSection(setupFAQ = setupFAQ, onFAQClick = ::launchFAQActivity, onEmailClick = {\n"
                "                                onEmailClick(onEmailClickAlertDialogState::show)\n"
                "                            })\n"
                "                        }\n"
                "                    },\n",
                "                    aboutSection = {},\n",
            ),
        ],
    )

    patch(
        "commons/src/main/kotlin/com/simplemobiletools/commons/compose/screens/AboutScreen.kt",
        [
            (
                "        SettingsListItem(text = stringResource(id = R.string.about_footer))\n",
                "",
            ),
            (
                "        SocialText(\n"
                "            click = onFacebookClick,\n"
                "            text = stringResource(id = R.string.facebook),\n"
                "            icon = R.drawable.ic_facebook_vector,\n"
                "        )\n"
                "        SocialText(\n"
                "            click = onGithubClick,\n"
                "            text = stringResource(id = R.string.github),\n"
                "            icon = R.drawable.ic_github_vector,\n"
                "            tint = SimpleTheme.colorScheme.onSurface\n"
                "        )\n"
                "        SocialText(\n"
                "            click = onRedditClick,\n"
                "            text = stringResource(id = R.string.reddit),\n"
                "            icon = R.drawable.ic_reddit_vector,\n"
                "        )\n"
                "        SocialText(\n"
                "            click = onTelegramClick,\n"
                "            text = stringResource(id = R.string.telegram),\n"
                "            icon = R.drawable.ic_telegram_vector,\n"
                "        )\n",
                "        SocialText(\n"
                "            click = onGithubClick,\n"
                "            text = stringResource(id = R.string.github),\n"
                "            icon = R.drawable.ic_github_vector,\n"
                "            tint = SimpleTheme.colorScheme.onSurface\n"
                "        )\n",
            ),
        ],
    )

    scrub_simplemobiletools_com()
    print("commons fork patches applied")


if __name__ == "__main__":
    try:
        main()
    except SystemExit as e:
        print(e, file=sys.stderr)
        raise
