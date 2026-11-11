# Sample Media Data

File: `app/src/main/assets/sample/library.json` (`version: 1`).
This dataset is used for class demos, the intro video, and development testing. It simulates one year (2025-11 to 2026-10) of viewing history for a single user. All overviews are written in my own words and do not copy text from Douban or TMDB. No downloaded posters are used (`poster` is `null` everywhere, so the app shows a placeholder).

The entire dataset is in English, as required by the project's English-only UI rule (see `AGENTS.md`).

## Totals

| Category | Count |
| --- | --- |
| Movies | 20 |
| Series | 10 |
| Total | 30 |

## Status distribution

| Status | Count |
| --- | --- |
| watched | 14 |
| want | 8 |
| watching | 5 |
| dropped | 3 |

## Genre coverage

10 genres are used (requirement is at least 6): Drama, Sci-Fi, Action, Mystery, Crime, Romance, Animation, Comedy, Fantasy, War.

## Rating distribution

- 3 titles at 9.5 or above: The Shawshank Redemption 9.7, Farewell My Concubine 9.6, Interstellar 9.5.
- 3 titles below 5: Avengers: Endgame 4.0, Ready Player One 4.5, The Big Bang Theory 4.5 (all dropped).
- Most fall in the 6-9 range; the 8 "want" titles are unscored (`rating` is `null`).

## Viewing records

- Record dates span **2025-11 to 2026-10**, with at least one record in each of the 12 months.
- Records per month: 2025-11(1), 2025-12(1), 2026-01(8), 2026-02(3), 2026-03(4), 2026-04(5), 2026-05(6), 2026-06(4), 2026-07(3), 2026-08(4), 2026-09(4), 2026-10(2). January and May are clearly heavier, so the charts show peaks.
- **Streak**: 2026-01-05 to 2026-01-12 is 8 consecutive days, each with a viewing record (bingeing Game of Thrones).
- **Rewatch with changed rating**: 13 titles have 2 or more records with different ratings (e.g., Interstellar 9.0 to 9.5, Inception 8.5 to 9.0, Parasite 8.5 to 8.8, Spirited Away 8.8 to 9.0).