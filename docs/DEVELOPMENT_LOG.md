# Echo Relics 開発記録

最終更新: 2026-07-27

## 現在のマイルストーン

P0受け入れ検証 — 実クライアント・マルチプレイ・再起動ゲート

## M0 — 現状調査

### 完了した作業

- `docs/ECHO_RELICS_MASTER_SPEC.md` と既存の Echo Blade MVP を確認した。
- リポジトリ全体、登録方式、リソース、依存関係、ビルド、GameTest、Git、専用サーバー起動を確認した。
- 環境調査、仕様監査、マルチプレイ・安全性、検証設計を別々の担当で実施し、主エージェントが結果を統合した。
- 既存 MVP が、元の対象を保持せず、記録位置の局所 AABB を再検索する方式であることを確認した。

### 対象環境

- Minecraft Java Edition: `26.2`
- NeoForge: `26.2.0.28-beta`
- ModDevGradle: `2.0.142`
- Gradle Wrapper: `9.2.1`
- Java コンパイルターゲット / Toolchain: `25`
- Gradle ランチャー: Java `21`
- 直接依存: NeoForge のみ
- Mod ID: `echorelics`
- バージョン: `0.1.0`
- 登録方式: `DeferredRegister`、mod event bus、`NeoForge.EVENT_BUS`
- データ方針: 現時点は手書き JSON。データ生成プロバイダーは未導入。

### 既存 MVP から再利用する部分

- `EchoRecord`、優先度キュー、一時ストア、サーバー単位スケジューラー
- `EchoAction` と `EchoShape` の責務分離
- 回転直方体による固定空間斬撃
- 独自 Damage Type と `hurtServer` による再生ダメージ
- サーバー配信の粒子・音
- Echo Blade、2種のエンチャント、Creative タブ、レシピ、英日翻訳
- プログラム登録式 GameTest

### 仕様と実装の主な差

- Echo Sigil、Echo Plate、Resonance Target、Archive Door が未実装。
- Grand Echo Archive と Archivist が未実装。
- 敵・装置が所有する残響を扱えず、既存基盤は `ServerPlayer` に固定されている。
- マスター仕様の導入例で必要な「空振りした空間への斬撃記録」がない。
- 残響ダメージがノックバックを発生させ、固定空間から対象を押し出し得る。
- 所有者のペット除外、死亡直後の破棄、アクション例外境界が不足している。
- 独自のボス表示、モデル、テクスチャ、音、構造物がない。

### 技術的判断と理由

- 26.2 公式ドキュメントが未整備で、NeoForge も beta のため、実際の patched Minecraft sources と NeoForge sources を API の最終根拠にする。
- `Item#hurtEnemy` は主対象への成立攻撃をサーバー側で一度だけ捕捉する入口として維持する。
- 26.2 の `PlayerInteractEvent.LeftClickEmpty` はクライアント限定で、バニラの空振りパケットからサーバー側の攻撃イベントは発火しない。空振り記録には専用の小さな serverbound payload が必要。
- payload は入力通知だけに使い、位置、方向、武器、攻撃クールダウン、威力、エンチャントはサーバーで検証・取得する。クライアント値で攻撃結果を決めない。
- 残響はチャンクチケットを追加せず、発動地点が未ロードなら安全に消費する。
- P0 が全条件を満たすまでは P1/P2 に進まない。

### 仕様判断: 空振り斬撃の記録

- 変更したい内容:
  - 旧 Echo Blade MVP の「命中成立時だけ記録」に加えて、Echo Blade を持った十分にチャージ済みの空振りも記録する。
- 変更が必要な理由:
  - マスター仕様の導入、Echo Plate パズル、ボス戦の例は、現在敵がいない空間へ斬撃を置き、後からその空間を利用する遊びを要求している。
- 元の仕様を維持できない根拠:
  - 命中時だけでは「3秒前に空振りしておいた場所へ敵を誘導する」という中核デモを実行できない。
- プレイヤー体験への影響:
  - 狙った空間へ残響を事前配置できる。誤入力とスパムを防ぐため、サーバー側で武器、状態、手、攻撃クールダウンを検証する。
- 将来拡張への影響:
  - 対象 Entity を保存しない設計を強化し、弓、採掘、移動、装置操作など「空間へ行動を置く」拡張と整合する。
- 採用した代替案:
  - 通常命中は従来どおり `hurtEnemy` から記録し、空振りだけを client event + serverbound payload で通知する。MISS だけを送るため同一攻撃の二重記録を避ける。

### バランス判断

- マスター仕様の初期値 60–80% に合わせ、Echo Blade の既定残響倍率を `0.75` にする。
- Replay は武器エンチャント追加効果、耐久消費、炎上、通常攻撃コールバック、ノックバックを再現しない。

### 実行したコマンド

- `.\gradlew.bat --version`
- `.\gradlew.bat dependencies --configuration runtimeClasspath`
- `.\gradlew.bat tasks --all`
- `.\gradlew.bat build`
- `.\gradlew.bat runGameTestServer`
- `.\gradlew.bat runServer`
- `git status --short --branch`
- `git log -1 --oneline`

### ビルド結果

- 基準 `build`: 成功
- 基準専用サーバー: ログの `Done` まで成功。検証用プロセスは停止済み。

### テスト結果

- GameTest 1回目: `repeated_replay_kill` が失敗。
  - 残響ノックバックで固定空間から対象が動く不安定性と、同じ run directory を使う同時 Gradle 実行が重なった可能性がある。
- GameTest 単独再実行: 8/8 成功。
- 別担当の単独実行: 8/8 成功。
- 結論: 基準テストは通るが、ノックバックの仕様不一致とテストの不安定性は M1 で修正する。

### Git

- `.git` は存在する。
- ブランチは `master`、まだコミットはない。
- 既存プロジェクトファイルはすべて untracked。ユーザー指示がないためコミットは作成しない。

### 手動確認が必要な項目

- クライアント GUI 起動、アイテム描画、粒子・音、空振り入力感。
- 実クライアント2台による PvP、friendly-fire、ログアウト、死亡、ディメンション移動。

### 既知の問題・将来のリスク

- Actor と発生由来は一般化済みだが、敵・装置による実アクションは後続マイルストーンで初めて検証する。
- 候補 Entity 数には上限を追加したが、密集時の演出パケット数は実測が必要。
- 26.2 beta の API とデータ形式は更新で変わる可能性がある。ローダー更新は機能実装と分離する。

### 次の作業

- M2として Echo Sigil、固定位置の残像、Echo Plate、Resonance Target、Archive Door、一人用試験空間を実装する。

## M1 — 残響基盤と Echo Blade

### 対象範囲

- 既存MVPの固定空間斬撃を維持し、空振り設置を追加する。
- Actor、発生由来、対象方針を敵・装置へ拡張可能にする。
- no-knockback、ペット・同盟、死亡破棄、例外境界、件数上限を固める。

### 完了した作業

- `EchoActorRef`、Actor種別、Alignment、Provenance、実行コンテキストを追加した。
- スケジューラーをPlayer固定からPlayer／LivingEntity／Device対応へ一般化した。
- 空振り専用serverbound payloadを追加した。クライアントは入力だけを通知し、サーバーが主手、状態、攻撃クールダウン、位置、方向、威力、エンチャントを検証・取得する。
- 十分にチャージされた空振りを記録し、成功時にサーバー攻撃ゲージをリセットする。MISS専用イベントのためEntity命中入口とは重複しない。
- Echo Bladeの既定残響倍率を`0.75`へ変更した。
- `echo_slash`を26.2の`minecraft:no_knockback`タグへ追加した。
- 所有ペット、双方向の同盟、PvP、marker ArmorStandを対象から除外した。
- 死亡時の即時破棄、logout、dimension change、server stopの破棄を実装した。
- アクションのwarning、origin、chunk lookup、executeをレコード単位のRuntimeException境界へ入れた。
- `getChunkNow`を使い、チャンクを生成・強制ロードしないようにした。
- 1 replayあたりのLivingEntity候補を既定128件へ制限した。
- README、テスト手順、CurseForge説明を空振り設置へ同期した。

### 対象版API確認

- `LeftClickEmpty`はクライアント限定で、サーバーへは通知されない。
- `RegisterPayloadHandlersEvent`と`PayloadRegistrar#playToServer`を使用し、既定のメインスレッドハンドラで処理する。
- 同一接続では空振りpayloadがvanilla swing packetより先に送られ、サーバー側のチャージ値を記録後に両経路がリセットする。
- 26.2では`DamageTypeTags.NO_IMPACT`と`NO_KNOCKBACK`が分離されている。前者では移動を抑止できないため、実ソースと失敗テストに基づき後者を使用した。
- `Level#getEntities(..., maxResults)`で局所AABB列挙自体へ上限を適用した。

### 独立レビュー

- 判定: High 0件。Medium修正後にM2進行可。
- 修正したMedium:
  - `EchoAction#origin()`が例外境界外だった。
  - README／TESTING／CurseForge／開発記録が空振り実装と同期していなかった。
  - 空振りサーバー検証と実スケジューラー経路の自動テストがなかった。
  - 所有ペット対象方針の自動テストがなかった。
- Lowとして記録:
  - `copyable`はPhase 3までメタデータのみ。コピー生成の単一入口で強制する。
  - 異常Actionの大量失敗時ログは未レート制限。現行Actionでは再現せず、必要時にAction種別単位で制限する。

### 実行したコマンド

- `.\gradlew.bat compileJava`
- `.\gradlew.bat runGameTestServer`（修正前失敗、修正後3回）
- `.\gradlew.bat build`
- `.\gradlew.bat runServer`
- ログ検査と検証用プロセス停止

### ビルド結果

- `compileJava`: 成功
- `build`: 成功
- 生成物: `build/libs/echorelics-0.1.0.jar`
- 専用サーバー: `Done (0.293s)`まで成功。検証プロセスは停止済み。

### テスト結果

- 最初の追加テスト: 8成功、`echo_damage_has_no_impact` 1失敗。
  - 26.2でノックバック抑止は`no_impact`ではなく`no_knockback`であることを特定して修正。
- 修正後: 9/9成功。
- 独立レビュー修正後: 12/12成功。
- 反復実行: 12/12成功。
- 自動確認済み:
  - 回転形状、背後・横・高さ除外
  - 元対象非追跡、後入り対象、複数対象
  - 複数回再生と死亡済み対象の安全性
  - no-knockback
  - 実スケジューラーの予兆2回、再生2回、完了
  - Actor別上限・優先順・固定間隔
  - 空振りの主手、チャージ、成功後reset、即時重複拒否
  - 所有ペット除外
  - Creativeタブ、エンチャントI–III、エンチャントテーブルタグ

### 手動確認

- 未確認: 実クライアントでの空振り入力、通常命中との非重複、描画、粒子、音。
- 未確認: 2クライアント同時利用、PvP、team friendly-fire、logout、死亡、dimension change。
- コードと専用サーバーで確認: common側にclient importなし、payloadはサーバー検証、専用サーバー起動成功。

### 既知の問題・将来リスク

- 実二クライアントの挙動は未確認。
- 既存の`run/config/echorelics-server.toml`は旧値`damageMultiplier=1.0`を保持する。新規環境の既定値は`0.75`。
- 同tick密集時の演出パケット量は未プロファイル。
- 壁遮蔽は行わない。

### 次の作業

- M2の各装置をサーバー権威で実装し、残像だけが専用Plateへ反応するようにする。
- Resonance Targetへ残響アクションのブロック反応入口を追加する。
- 失敗後すぐ再挑戦でき、アンロード・再起動で不正状態を残さない試験空間を作る。

## M2 — Echo Sigil とパズル装置

### 対象範囲

- Echo Sigil、固定位置のEcho Avatar、Echo Plate、Resonance Target、Archive Doorを追加する。
- 一人でも「過去の自分」と二枚のPlateを同時に起動できる最小試験を作る。
- Bladeの残響からブロック装置へ反応させ、通常攻撃・敵性残響では標的を解かない。
- Archive本体の構造生成はM3へ分離する。

### 完了した作業

- Sigil使用時の位置・水平向きをサーバーで記録し、60 tick後に100 tick存在する固定残像を生成するようにした。
- Sigilへ160 tickのサーバークールダウンを設けた。
- 固定残像を非衝突・非保存・無敵・アイテム非取得・vanilla block trigger無効にし、所有者ごと同時1体へ制限した。
- Avatar ManagerがEntityへの直接参照とLevelの絶対失効時刻を保持し、AvatarのチャンクがEntity tick対象外になってもサーバーtickから期限どおり破棄するようにした。
- プレイヤーまたは固定残像だけが起動するEcho Plateを追加した。
- プレイヤー由来の残響だけが80 tick起動するResonance Targetを追加した。
- 扉の正面入力Target、または正面3ブロック・左右2ブロックの二枚のPlateだけを入力として読むArchive Doorを追加した。TargetはM3レビューで通路の2ブロック上へ移した。
- 装置状態変更時だけ半径8ブロック内の扉へ再評価を通知し、扉自身は上記の固定入力だけを読む。毎tickの全ワールド走査と、別パズル入力の混線を避けた。
- 回転斬撃の粗いAABB内だけブロックを列挙し、正確な形状交差後に残響対応ブロックを呼ぶ共通入口を追加した。
- 1 replayで検査するブロック数の設定上限を追加した。
- アイテム、ブロック状態、モデル、レシピ、loot table、pickaxeタグ、英日翻訳、クリエイティブタブ登録を追加した。

### 技術的判断

- M2の残像はゲーム判定用Entityと、vanilla player modelを半透明・発光色で描く専用Rendererで構成する。本人スキンの複製は行わず、外観は仮素材としてP0ポリッシュ対象にする。
- Avatarはvanilla pressure plateへ反応させず、Echo Plate自身が局所AABBで明示的に検出する。
- Doorは汎用論理回路やBlockEntityを持たず、向きに対して決まる三つの入力座標だけを読む小さな決定的ルールとした。8ブロック探索は状態変更を通知する扉の発見だけに使い、入力集約には使わない。
- 待機中SigilとAvatarはメモリ内のみで、死亡、logout、dimension change、server stopで破棄する。
- Resonance Targetは`EchoExecutionContext`のAlignmentを検査し、敵性残響によるパズル解除を防ぐ。通常攻撃にはブロック反応入口がない。

### 対象版API確認

- 26.2の`Item#use(Level, Player, InteractionHand)`、`InteractionResult.SUCCESS_SERVER`、`ItemCooldowns`を実ソースとコンパイルで確認した。
- 26.2の`EntityType#create(Level, EntitySpawnReason)`、`Entity#shouldBeSaved`、`isIgnoringBlockTriggers`、`hurtServer`、`SynchedEntityData.Builder`を実ソースとコンパイルで確認した。
- 26.2のBlock callback、scheduled tick、signal source、collision shape、`RegisterRenderers`を実ソースとコンパイルで確認した。
- 追加JSONはすべて構文解析し、26.2の単数形`loot_table`、`recipe`配置で専用サーバー読込を確認した。

### 実行したコマンド

- `.\gradlew.bat compileJava`
- `.\gradlew.bat runGameTestServer --no-daemon`（追加・レビュー修正・安定性確認）
- `.\gradlew.bat build --no-daemon`
- `.\gradlew.bat runServer --no-daemon`
- JSON構文、JAR内容、client import、専用サーバーログの静的検査

### ビルド・テスト結果

- `build`: 成功。`build/libs/echorelics-0.1.0.jar`を生成。
- GameTest追加直後: 14/15。mock playerの接続がない状態でcooldown同期が走る試験ハーネス固有の失敗を、test専用`ItemCooldowns`で修正した。
- 次の実行: M2試験は成功したが、既存`echo_damage_has_no_impact`が一度だけ失敗した。
- 原因を断定せず再確認し、形状・対象選別とDamageTypeタグ検証が同じ試験へ混在していたため、後者を`EchoDamageExecutor`の直接検証へ分離した。
- 初期安定化後: 15/15成功、GameTest本体831 ms、Gradle終了コード0。
- その前の一回は15/15成功後の終了処理がツール180秒上限を越えたが、次回29秒で正常終了し、恒常的な機能遅延は再現しなかった。
- 独立レビュー修正後、Entity追加と同tickの空間検索を行う試験がEntity登録境界を観測して不安定になることを確認した。機能側の対象追跡へ変更せず、試験の空間検索を次tickへ移した。
- 初回レビュー修正後GameTest: 19/19成功を2回連続で確認。実スケジューラーを通るSigil→60 tick待機→Avatar→二枚Plate→Door→Avatar自然消滅→Door閉鎖を含む。
- 最終レビュー修正後GameTest: 21/21成功。Entity tickなしでもManagerが絶対時刻でAvatarを失効させる試験と、Device設置・破壊、Target再命中後の80 tick再起算を追加した。
- 最終`build`: 成功。`build/libs/echorelics-0.1.0.jar`を生成。
- 専用サーバー: レビュー前に`Done (0.255s)`、レビュー修正後に`Done (0.302s)`まで成功し、ワールド保存を確認。各検証プロセスは停止済み。
- JSON構文: 全件成功。
- JAR: M2の登録・モデル・レシピ・loot tableを収録し、旧`examplemod` IDなし。

### 自動確認済み

- Sigilの成功、cooldown中拒否、固定位置のAvatar生成。
- Avatarの非保存、vanilla block trigger無効、所有者ごと一体への置換。
- player-aligned echoによるTarget起動とDoor開閉、敵性残響の拒否。
- AvatarによるPlate起動、無関係Mobの拒否、二枚同時でDoor開放、一方解除時の閉鎖。
- Sigilの実スケジューラーから生成されたAvatarと現在のPlayerによる二枚Plate攻略、寿命切れによる自然解除。
- 二人のserver-listed mock playerによる二枚Plate攻略、片方退出時の所有者分離と閉鎖。
- 所有者ごとのAvatar置換分離。
- Doorの固定入力座標と、近傍の無関係なPlateを集約しないこと。
- Target再命中による80 tick期限の更新。
- ブロック反応・扉通知が未ロードチャンクをロードしないこと。
- EntityをLevelへ追加しない条件でも、ManagerのサーバーtickだけでAvatarを絶対失効時刻に破棄すること。
- powered Targetの設置・破壊がDoorを即時再評価し、60 tick時点の再命中後は旧期限を越えて起動を維持し、最新命中から80 tick後に解除すること。
- M1の固定空間、非追跡、複数対象、無限連鎖防止、スケジュール、エンチャントを含む全回帰。

### 手動確認

- 未確認: 実クライアントのSigil入力、固定残像の視認性、全アイテム・モデル表示、音量。
- 未確認: 実プレイヤーとAvatarによる二枚Plate攻略の操作感、時間猶予、再挑戦。
- 自動確認: 同一専用サーバー内の二人のserver-listed mock playerによるPlate攻略と片方退出時の分離。
- 未確認: GUIを持つ実クライアント2台での同時Sigil、二人Plate攻略、見た目、ネットワーク遅延下の操作感。

### 既知の問題・将来リスク

- 固定残像は半透明のvanilla player modelとサーバー粒子を使う仮外観で、本人スキンは複製しない。
- Doorは単一ブロック高のため飛び越えられる。M3の構造側で通路全高を塞ぐ配置にする。
- Device変更通知は小さな局所走査だが、各AvatarがPlate上で10 tickごとに状態を再確認する。大量設置時は実測が必要。
- 実二クライアントと実クライアント描画は未確認であり、完了済みとは報告しない。
- Plate、Target、Doorのmodelと音はvanilla素材による仮素材である。

### 独立レビュー

- 初回判定: High 4件、Medium 6件、Low 4件。High修正前はM3へ進行不可。
- 修正したHigh:
  - Sigilから自然消滅までの統合経路を実スケジューラーGameTestへ追加した。
  - Doorの半径内入力集約を廃止し、向きに対する固定入力座標へ変更した。
  - ブロック走査・通知・入力読取へ`isLoaded`ガードを追加し、未ロードチャンク非読込試験を追加した。
  - server-listed mock player二人による同時Plate・退出分離試験を追加した。実GUI二台は手動未確認として明記する。
- 修正したMedium:
  - Target再命中で既存scheduled tickを消去し、80 tickを再起算する。
  - Avatar消滅後のPlate再評価を次tickへ確実に予約する。
  - Device設置・破壊でもDoorを再評価する。
  - NoopRendererを半透明・発光色のplayer model rendererへ置換した。
  - 将来用Provenance名をActorとAlignmentの組合せが誤読されない表現へ変更した。
  - README、TESTING、開発記録を実装へ同期した。
- 再レビュー判定: High 0件、Medium 2件。次の修正と再検証後にM3進行可。
  - チャンクのEntity tick停止でAvatar寿命が止まらないよう、Managerに直接参照と絶対失効時刻を持たせ、サーバーtickで失効させた。消滅時Plate補助関数にも`isLoaded`ガードを追加した。
  - Target期限更新とDevice設置・破壊の専用GameTestを追加した。
  - 修正後は全21件のGameTestと`build`が成功した。
- Lowとして記録:
  - Door一個では飛び越え可能。M3構造配置で対処する。
  - M2装置レシピはテスト用入手経路であり、Archive報酬導線との整理が必要。
  - Avatar粒子量とvanilla仮素材はP0ポリッシュで実測・更新する。

### 次の作業

- M3で固定構成のGrand Echo Archive、導入、三つの試練、報酬部屋、再挑戦経路を実装する。

### 完了判定

- 独立再レビュー最終判定: High・Medium解消、M3進行可。
- `runGameTestServer`: 21/21成功。
- `build`: 成功。

## M3 — Grand Echo Archive

### 対象範囲

- 自然生成する固定構成の遺跡本体を追加する。
- 導入、空間斬撃、過去の自分、残響戦闘、最終書庫、物資報酬、退出を一つの前進経路にまとめる。
- 死亡、時間切れ、ログアウト後も入力側から再挑戦できる構造にする。
- Archivist本体とボス報酬はM4で統合する。

### 完了した作業

- 26.2の`Structure`、`StructurePiece`、`StructureType`、`StructurePieceType`を使う手続き生成構造を追加した。
- 幅17、高さ9、奥行109の固定Archiveを、9点の地表高差が4ブロック以内の候補へ配置する。
- 平原、ヒマワリ平原、サバンナへ、spacing 28・separation 12で生成するStructure Setを追加した。
- deepslate、tuff、風化銅、amethyst、tinted glass、bookshelfを使い、反復アーチ、同心円、壊れた時計床、両端の塔を構成した。
- 入口と反対側の両方に開口を持たせ、時限扉の奥で袋小路へ閉じ込められない経路にした。
- 中央経路を塞がない左右一対の入口cacheへ最大16本のEcho Blade、第一試練後の一対のcacheへ最大16個のEcho Sigil、最終物資室へ左右一対のamethyst、diamond、food/experience報酬を追加した。
- 第一試練は空振り斬撃の再生でTargetを起動、第二試練はSigil Avatarと現在のPlayerで二枚Plate、第三試練は三体の永続ZombieとTargetを使う。
- Archive Doorのcollisionとblock modelを二ブロック高へ拡張し、飛び越しによる試練回避を防いだ。
- Resonance Targetを通路の2ブロック上へ移し、開扉後の足元と頭上を空けた。
- Echo Plate解除へ40 tickの通過猶予を追加し、プレイヤーが扉内にいる間は閉鎖を10 tickずつ延期するようにした。
- 閉じたDoorの新規開放は二枚のPlateが`POWERED && !RELEASING`である場合だけ許可し、すでに開いたDoorの保持だけに40 tick猶予を使う。これによりSigilなしの順踏み攻略を拒否する。
- 開いたDoorのcollisionとselection shapeを空にし、通過・照準・設置を不可視面で遮らないようにした。
- Guard生成済みbit maskをStructure Piece NBTへ保存し、同じPieceのchunk分割生成で重複spawnしないようにした。
- Guard生成とbit更新を同一Piece単位で同期化し、隣接chunk生成が並行した場合のbit lost updateを防いだ。

### 技術的判断

- 一つの完成度が高い固定構成を優先し、Jigsaw poolや部屋ランダム化はP1へ送った。
- binary NBT templateを配布せず、Javaの手続きPieceとdatapack registry JSONを組み合わせる。差分レビューと対象版API確認が容易になる。
- 一つのPieceはchunkごとにBoundingBoxでclipされる。構造生成時だけ局所的に処理され、通常server tickでは探索しない。
- Echo処理は引き続きchunkをロードしない。Archive実配置GameTestだけは、vanilla`/place structure`が要求する全BoundingBoxを一時的にロードし、終了時にticketを解除する。
- 固定構造は入口から出口まで前進可能にし、時限扉の後ろへ戻れなくても進行不能にならない。死亡時は外側から装置を再入力できる。
- 長い水平構造が急傾斜へ埋没・浮遊しないよう、幅・中央・両端を組み合わせた9点を対象版Heightmap APIで採取し、高低差が4を超える候補を生成前に拒否する。
- M3の最終cacheは開発中の物資報酬で、Archivist撃破報酬ではない。M4でボス部屋と最終報酬制御を追加する。
- Echo Sigilと装置の既存recipeはテスト・sandbox入手経路として維持するため、遺跡報酬を取らずに試練を再現することもできる。

### 対象版API確認

- 26.2の`ChunkPos`はBlockPos constructorではなく`ChunkPos.containing`を使う。
- 銅系Blockは個別の旧定数ではなく`WeatheringCopperCollection`からstateを選ぶ。
- `WorldGenLevel#addFreshEntityWithPassengers`は`void`である。
- 26.2の動的データ配置は`data/<namespace>/worldgen/structure`と`structure_set`、lootは単数形`loot_table`である。
- vanilla`PlaceCommand`は生成Structureの全BoundingBox chunkがロード済みであることを検査するため、実配置GameTestを別environmentへ隔離した。
- `ChunkGenerator#getFirstOccupiedHeight`、`Heightmap.Types.WORLD_SURFACE_WG`、`GenerationContext#heightAccessor/#randomState`を26.2 patched sourceとコンパイルで確認した。

### 実行したコマンド

- `.\gradlew.bat compileJava --no-daemon`
- `.\gradlew.bat runGameTestServer --no-daemon`（実装・失敗原因調査・隔離後）
- `.\gradlew.bat build --no-daemon`
- `.\gradlew.bat runServer --no-daemon`
- JSON構文検査、JAR内容検査、専用サーバーログ検査

### ビルド・テスト結果

- `compileJava`: 成功。
- GameTest:
  - 動的Registry追加後は22/22成功。
  - 最初の`/place`試験は未ロード位置を正しく拒否し失敗。
  - 起点だけの同期loadではStructure全域のload要件を満たさず失敗し、同時chunk生成が既存Entity試験3件のtick境界も乱した。
  - 実配置を専用environmentへ分離し、正確なBoundingBox交差chunkへ一時ticketを付けた後は23/23成功。
  - ログで`Generated structure "echorelics:grand_echo_archive"`を確認。
  - 独立レビュー修正後は24/24成功。実配置後の固定座標、通路衝突、3扉、2標的、2Plate、6 chest、3 guardと、扉内閉鎖延期を追加検証した。
  - 最終High修正後は25/25成功。Sigilなし順踏み拒否、Avatar＋現在Playerの同時成立、猶予からactiveへ戻ったPlateのDoor再通知を追加検証した。
  - 開扉selection shape追加後も25/25成功。GameTestサーバー完了は2.177分で、全必須試験成功・終了コード0をログ末尾で確認した。
- `build`: 成功。`build/libs/echorelics-0.1.0.jar`を生成。
- 独立レビュー修正後`build`: 成功。`build/libs/echorelics-0.1.0.jar`を再生成。
- 専用サーバー: 初回`Done (0.281s)`、レビュー修正後`Done (0.288s)`へ到達。各検証プロセスは停止済み。
- JSON: 全件構文解析成功。
- JAR: Structure class、動的Registry、biome tag、3 loot tablesを収録。

### 自動確認済み

- StructureとStructure Setのdynamic registry decode。
- Pieceの17×9×109 BoundingBox。
- 専用Server CommandSourceからの実`/place structure`成功。
- 実配置後の中央通路がTarget・chestで塞がれず、Target起動後のDoor collisionが空になること。
- Plate解除後の40 tick猶予と、プレイヤーが扉内にいる間の閉鎖延期。
- 閉じた二枚Plate Doorは一人の順踏みでは開かず、PlayerとAvatarが同時に踏むと開くこと。
- 開いたDoorのcollisionとselection shapeがどちらも空であること。
- Archiveの3扉、2標的、2Plate、6 chest、3 guardの固定配置。
- 実配置試験の一時chunk ticket解除。
- M1/M2を含む全25件の回帰。

### 手動確認

- 未確認: 通常Overworldでの自然生成、`/locate`、地表へのなじみ、傾斜・水・他構造との重なり。
- 未確認: client上の外観、照明、二ブロックDoor model/collision、三つの試練を最初から出口まで攻略。
- 未確認: 実プレイヤー二人での共有loot、時限扉通過、死亡・再接続・再挑戦。

### 既知の問題・将来リスク

- 固定構成のみで部屋バリエーションはない。
- 入口cacheとSigil cacheは各二個・計16専用品へ増量したが共有containerであり、全量持ち出し後の後発参加者へ保証しない。recipeと`/give`が回避策。
- 大きな単一Pieceは生成時に複数chunkへまたがる。GameTest実配置では成功したが、通常worldgenの地形生成時間は実測が必要。
- 9点の高低差が4を超える急傾斜候補は拒否するが、点間の局所的な穴・張り出し・水面は自然生成の手動確認が必要。
- 通常Overworldへ`/locate`を自動入力する試行はサーバー起動前に入力が消費され、応答を取得できなかった。別のバックグラウンド停止方式は無関係なJavaプロセスを巻き込む危険があるため安全審査で拒否され、迂回しなかった。自然生成成立率は未確認のまま手動項目とする。
- GameTestの`/place`は固定の低いTest Level地形でpost-processing警告を出すが、構造生成と全試験は成功する。
- 第三試練のZombieはP0用vanilla仮敵で、Remnantではない。
- Archivist arena、boss gate、撃破報酬はM4で追加する。

### 独立レビュー

- 初回判定: High 2件、Medium 5件、Low 2件。High修正前はM4へ進行不可。
- 修正したHigh:
  - 唯一の通路に置かれていた第一・第三Targetを2ブロック上へ移動し、実配置後のplayer-sized passageとDoor collisionをGameTestへ追加した。
  - Plate解除から閉扉までの猶予を40 tickへ延長し、Door AABB内にPlayerがいる場合は閉鎖を延期する安全策とGameTestを追加した。
- 修正した対象範囲内Medium:
  - 自然生成biomeを比較的平坦な3種へ絞り、9点の地表高差が4を超える候補を拒否する。
  - 進行用lootを通路外の左右二箱、各8個へ増量した。
  - `/place`後の主要装置、供給、Guard数、通路・衝突を固定座標で検証する。
  - READMEの「complete route」を自動確認範囲と実クライアント未確認が分かる表現へ修正した。
- Low／将来課題:
  - 単一Pieceの生成ループ負荷は通常worldgenで実測する。
  - 二ブロックDoor modelの見た目と実クライアント通し攻略は未確認。
  - 第三試練のGuardはTargetの強制kill gateではない。通常戦闘を許すP0仕様として維持し、M4の戦闘ゲートと分離する。
- 再レビューで追加High 1件:
  - 40 tick猶予中も`POWERED`であるPlateをDoorが新規開放入力として読んでいたため、一人の順踏みでSigil試練を回避できた。
  - 新規開放は二枚とも`!RELEASING`を要求し、開放後だけ猶予を許可した。graceからactiveへ戻る際もDoorを再通知する。
  - 順踏み拒否とPlayer＋Avatar同時成立を専用GameTestへ追加し、25/25成功と`build`成功を確認した。
- 再レビューMedium対応:
  - 開扉中のselection shapeを空にした。
  - Guard bit更新を同期化した。
  - 反対側開口からM4報酬を迂回できる問題は、M4のboss gate設計で必ず閉じる。
  - 自然生成成立率は自動確認できず、手動未確認として維持する。

### 完了判定

- 独立レビューのHigh 3件をすべて修正し、対象範囲内Mediumを修正または明示した。
- `runGameTestServer`: 25/25成功。
- `build`: 成功。M4進行可。

### 次の作業

- M4でArchivistの基本AI、敵性空間斬撃の再生、残響Targetによる防御解除、boss gate、報酬、死亡・再挑戦を実装する。
- Phase 1と2が安定するまでPhase 3/P1へ進まない。

## M4 — Archivist

### 対象範囲

- 最終書庫へ一体だけ生成されるArchivist、基本近接AI、Boss Barを追加する。
- Phase 1として、ボスの成立した近接斬撃を過去の固定空間へ敵性残響として再生する。
- Phase 2として、体力半分以下で通常ダメージを拒否し、二つのResonance Targetをプレイヤー由来の残響で同時起動すると解除される結界を追加する。
- 撃破後の恒久ゲート解放、P0報酬、専用進捗、保存・再挑戦経路を追加する。
- Phase 3のプレイヤー残響コピーは、Phase 1・2とP0の実クライアント検証後に判断するP1機能として分離する。

### 完了した作業

- `ArchivistEntity`を登録し、120 HP、近接攻撃、32 block索敵、9 blockのhome制限、紫/赤へ変化するBoss Barを実装した。
- 成立した近接攻撃一回につき、攻撃時のボス位置と水平攻撃方向から一件の敵性`SlashEchoAction`を作り、60 tick後に再生する。
- 敵性残響は共通の`EchoScheduler`、`EchoShape`、`EchoTargetPolicy`、`EchoDamageExecutor`を使う。最初に殴ったEntityのID・参照を命中対象として保存しない。
- 敵性残響の対象を再生空間内の生存Playerへ限定し、player-aligned残響と異なる赤橙系の予兆・発動演出を追加した。
- 体力50%以下で一度だけ結界へ入り、通常ダメージを拒否する。アリーナhomeから固定オフセットの二つのResonance Targetが同時に起動すると恒久的に解除する。
- 結界中に二つのTargetが欠損していれば、ロード済みの固定座標だけへ復元し、破壊や失敗で進行不能にならないようにした。
- home、報酬ゲート、出口ゲート、結界発動済み・解除済み状態をEntity NBTへ保存する。
- Grand Echo Archiveの最終区画へArchivist一体、二つの結界Target、前後二つのboss gateを配置した。Structure Pieceにspawn済みbitを保存し、chunk分割生成時の重複を防いだ。
- ボス死亡時だけ二つのboss gateへ`BOSS_UNLOCKED=true, OPEN=true`を保存する。Target解除や近傍入力更新後も閉じない。
- 報酬としてダイヤモンド剣相当の`Awakened Echo Blade`、アメジスト24個、challenge進捗`echorelics:defeat_archivist`を追加した。
- 強化版Bladeを通常版と同じ`EchoBladeItem.isEchoBlade`入口へ統合し、空振り・成立攻撃の空間記録、エンチャント、`minecraft:swords`タグを共有する。
- 専用CreativeタブとCombatタブへ強化版Bladeを追加した。ボス、強化版Blade、進捗の英日翻訳を追加した。
- Archivist用の仮描画として、発光色を付けたvanilla player model rendererをCLIENT登録した。独自ライセンス素材は追加していない。
- Archive Doorの新しい`boss_unlocked`を含む全16 blockstate variantへモデルを明示した。
- READMEと`docs/TESTING.md`をM4実装、P1境界、既知の制限、実クライアント/実二クライアント手順へ同期した。

### 技術的判断

- 敵性残響も共通基盤の`EchoActorRef.livingEntity`と`HOSTILE_RECORDED` provenanceを使い、専用の対象追跡ダメージ経路を作らない。
- hostileかどうかは初期演出差分として`SlashEchoAction`へ保存する。形状、予定tick、威力、予兆はスケジュール時に固定され、後のボス位置やtarget変更で再計算しない。
- ボス結界の判定座標はhomeから決め、現在のボス位置から追従させない。パズル空間と攻撃空間を同じ「固定された場所」の規則へ揃える。
- boss gateをアリーナ前後の二枚にし、反対側入口から報酬を迂回取得できない構造にした。
- `BOSS_UNLOCKED`は一時入力と独立したBlockStateへ保存し、datapack/SavedDataを増やさず再起動後も恒久解放を維持する。
- gateやsealの操作でチャンクを強制ロードしない。ボスアリーナ内では通常ロードされるが、管理者がボスを遠隔killする特殊ケースは既知の制限とする。
- P0報酬は新しい公開registry IDを持つ強化Bladeと進捗に限定し、Phase 3や新しい強化GUIを先取りしない。

### 対象版API確認

- 26.2の`Monster#doHurtTarget(ServerLevel, Entity)`、`customServerAiStep(ServerLevel)`、`ServerBossEvent`、`startSeenByPlayer/stopSeenByPlayer`をコンパイルと専用サーバーで確認した。
- 26.2のEntity保存は`ValueInput` / `ValueOutput`を使用し、BlockPosは子objectのX/Y/Zとして保存した。
- 真の死亡判定は`LivingEntity#die`後のprotected `dead`を確認してからgateを開く。キャンセルされた死亡で報酬経路を解放しない。
- entity lootは`data/echorelics/loot_table/entities/archivist.json`、進捗は`data/echorelics/advancement/defeat_archivist.json`として読み込まれる。
- SoundEvent定数は26.2 patched sourceで確認し、敵性記録演出に`TRIAL_SPAWNER_OMINOUS_ACTIVATE`を使用した。

### 実行したコマンド

- `.\gradlew.bat runGameTestServer --no-daemon`
- `.\gradlew.bat build --no-daemon`
- `.\gradlew.bat runServer --no-daemon`
- `.\gradlew.bat runClient --no-daemon`
- JSON構文検査、サーバーログ検査、生成JAR検査、Javaプロセス終了確認

### ビルド・テスト結果

- GameTest初回: 27/27成功。完了時間47.09秒、Gradle終了コード0。
- 独立レビューのphase gate修正直後: 26/27。1000 damage検証と解除後damage検証を同一tickへ置いたため、vanillaの被ダメージ無敵時間が後者だけを拒否した。機能側を変えず、試験側で無敵時間終了を明示した。
- 独立レビュー修正後GameTest: 27/27成功。完了時間3.681秒、Gradle終了コード0。
- 進捗付与検証追加後GameTest: 27/27成功。接続済みmock playerに`No History Is Final` challenge完了ログが出て、PlayerAdvancementsの完了状態もassertした。構造試験の実行順により完了時間2.124分、Gradle終了コード0。
- 最終`build`: 成功。`build/libs/echorelics-0.1.0.jar`（526,341 bytes）を生成。
- 専用サーバー初回: Echo Relics 0.1.0、Minecraft 26.2、NeoForge 26.2.0.28-betaとして`Done (0.273s)`へ到達。
- 独立レビュー修正後の専用サーバー: `Done (0.311s)`へ到達し、正常保存した。
- 専用サーバーは1590 recipes、1689 advancementsを読み込み、Archivistのentity type、loot、advancement、blockstateに起因するERROR/FATAL/registry decodeエラーなし。
- 時間制限後に残った今回開始のGradle/Java PID三件だけを特定して停止した。既存のJavaプロセスには触れていない。
- クライアント起動スモーク: OpenGL、Echo Relicsを含むResourceManager、音声、block/item atlas読込まで到達。missing model、renderer例外、Echo Relics由来のERRORなし。画面上の見た目は未評価。
- JSON: 全44件の構文解析成功。M4のloot/advancementを含むGameTestサーバーdata reloadに成功。
- 実再起動を標準入力で自動化する試行は、ModDevGradleの`runServer`がwrapperへ送った入力をゲームサーバーへ転送しなかったため不成立。コマンド実行・一時配置・ワールド変更は0件で、起動プロセスツリーだけを停止した。信頼できない試験スクリプトは削除し、再起動は手動ゲートに維持した。

### 自動確認済み

- ボスの成立近接攻撃から一件の敵性残響が作られ、元対象が離れても追跡せず、後から同じ記録空間へ入った別Playerへ命中する。
- hostile actionが通常のEcho Blade記録入口へ戻らず、無限連鎖を作らない。
- 体力半分で結界へ入り、通常ダメージを拒否する。
- HP50%超から1000の通常ダメージを受けても死亡・loot・gate解放へ進まず、HP50%で必ずPhase 2へ入る。
- 二つのplayer-aligned Resonance Targetが同時に起動した場合だけ結界を恒久解除し、その後は通常ダメージを受ける。
- 真の撃破でboss gateが開き、Target解除後も`BOSS_UNLOCKED`により開状態を維持する。
- 撃破lootへAwakened Echo Bladeが含まれる。
- Awakened Echo Bladeが共通Echo Blade型判定と`minecraft:swords`タグを持つ。
- 専用進捗が専用サーバーのAdvancementManagerへ読み込まれる。
- 接続済みServerPlayer由来のDamageSourceでArchivistを撃破すると、専用進捗が実際に完了する。
- 実`/place structure`後にArchivistが厳密に一体、二つのboss seal、合計五枚のDoor、前後boss gate、報酬cacheが固定位置へ存在する。
- M1〜M3を含む全27件の回帰。

### 手動確認が必要な項目

- 未確認: 実クライアントでのArchivist model、発光色、Boss Bar、近接AI、home復帰、敵性予兆の位置・方向・視認時間、音量、進捗toast。
- 未確認: 一人でArchive入口からArchivist撃破・報酬取得・退出まで通す実プレイとバランス。
- 未確認: 実クライアント二台でのhealth/shield/Boss Bar同期、別Playerへの敵性残響再検索、共同Target解除、共有loot。
- 未確認: ボス戦中の死亡・ログアウト・再接続、結界中と撃破後の専用サーバー再起動、重複spawnなし。
- 未確認: 通常Overworldでの自然生成と`/locate`、アリーナ前後からの実際の迂回不能。
- 未確認: Peacefulでの戦闘体験。Entityは存在可能だがMonsterのPlayer攻撃選択は難易度規則の影響を受ける。

### 既知の問題・将来リスク

- Archivistの外観、粒子、音、Awakened Echo Bladeのtextureはvanilla資産または通常Bladeを再利用した仮素材である。
- ボス死亡時のgate解放はチャンクを強制ロードしない。通常アリーナ戦では二枚とも近距離だが、管理者がボスを移動・遠隔killしてgate chunkをアンロードした場合は閉じたままになる。
- 実クライアントの近接AIと予兆視認性、実二クライアントの同期は自動試験では代替できない。
- 同時tickに複数残響が当たる場合はvanillaの無敵時間が後続ダメージを軽減する。強制回避しない。
- Entity lootは共有dropであり、各参加者への個別報酬ではない。
- shield中は固定二座標だけを確認し、ワールド全体探索はしない。pending recordがない場合のEcho基盤もEntity検索しない。

### 独立レビュー

- 初回指摘: High 1件、Medium 3件、検証不足。
- 修正したHigh:
  - HP50%超からの致死的な通常ダメージが`super.hurtServer`内で先に死亡を成立させ、Phase 2を飛ばせた。非bypass damageをphase境界までに制限し、成功後HPを50%へ固定して結界へ移行する。1000 damage、非死亡、gate閉鎖のGameTestを追加した。
- 修正したMedium:
  - NBT読込時にactive shieldからBoss Barの赤色を復元する。
  - home半径を16から9へ縮小し、二つのsealとreward gateを含みながら第三扉の外を除外する。
  - 敵性予兆を中心のランダム粒子だけでなく、記録originからreachまでの前方線と`halfWidth`全幅の横線で描く。
- 最終判定:
  - 最新コード上のCritical、High、M4範囲内で修正可能なMediumは0件。
  - 固定空間、元対象非追跡、無限連鎖防止、単一boss、サーバー権威、局所探索、上限は適合。
  - M4コードはGameTest、build、専用サーバー再検証後Go。
  - P0完了とP1着手は、実二クライアント、実サーバー再起動、実クライアント通し攻略が通るまでNo-Go。
- 残るLow:
  - 管理者がボスをgate chunk外へ移動して遠隔撃破した場合、チャンク非強制ロード方針によりgate解放をスキップする。
  - Peacefulではboss Entityが残る一方、難易度規則によりPlayerへ攻撃しない。
  - 仮renderer、共用Blade texture、参加者共有の単一entity loot。
- 追加対応:
  - 接続済みServerPlayer由来の撃破へ試験を変更し、進捗条件の実発火とPlayerAdvancements完了を自動確認した。toastの画面表示だけは手動項目に残す。

### 次の作業

- 実クライアント一台で自然生成、全試練、Archivist、報酬、進捗、退出まで通す。
- 実クライアント二台で敵性残響の別Player再検索、共同seal解除、health/shield/Boss Bar、死亡・再接続を確認する。
- shield中と撃破後に専用サーバーを再起動し、boss/seal/gateの保存、重複spawnなしを確認する。
- P0ゲート通過後にのみP1優先順位を再評価する。

## P0完成条件監査

### 自動確認済み

- Echo Bladeが空間へ斬撃を記録し、元の敵を追跡せず、後から範囲へ入った別対象へ命中する。
- ReverberationとAccelerandoが回数・間隔へ反映され、記録後の状態変更で既存記録が変わらない。
- Echo Sigilが固定位置へ過去の自分を残し、現在のPlayerと二枚のEcho Plateを同時起動できる。
- Resonance Targetがplayer-aligned残響に反応し、通常攻撃・敵性残響を拒否する。
- Grand Echo Archiveを専用サーバーの実`/place structure`で生成し、入口からboss区画までの装置・通路・供給・前後gateを固定座標で検査した。
- Archivistが固定空間の敵性残響と二標的結界という二つの固有ギミックを持ち、結界を解除して撃破・報酬・恒久gate解放へ進める。
- 接続済みServerPlayerがArchivistを撃破すると専用challenge進捗を取得する。
- logic server上のmock Player二人による所有者分離、二人Plate、退出処理を確認した。
- client側リソースとrendererは実クライアント起動時に例外なく読み込まれた。
- 専用サーバーは最終コードで`Done`へ到達し、全27 GameTestと`build`が成功した。
- README、既知の問題、具体的な英語手動テスト手順を更新した。

### 未確認のためP0完了を保留

- 普通のOverworldでの自然生成と`/locate`、地形への接続。
- 実クライアントでの残響位置・方向・危険範囲の視認性と全区間通し攻略。
- 実二クライアントでの共同攻略、別Playerへの敵性残響再検索、Boss Barとphase同期。
- shield中の再起動、撃破後の再起動、再接続後のgate永続化、重複Archivist不在。
- 実クライアント上のArchivist進捗toast表示。

### 判定

- M0〜M4のコード実装は完了。
- 自動化可能なP0回帰は成功。
- 実マルチプレイ、再起動、通し攻略を未確認のため、P0を「完成」とは報告せずP1へ進まない。
