// v. 3.0 by nepret(c) from ZuluHotel
// Stealth version 8.10.2
//22.2020

// !!! СКРИПТ БУДЕТ РАБОТАТЬ НА ВЕРСИИ СЕТЛСА НИЖЕ 8.11.X
// !!! это связано с тем, что в 8.11.x версиях есть баги которые пока что не пофикшены! ждём версии 8.11.3 и выше :\
Program Mining_BS_Tinkering_Sell;
const
{
Скрипт писался для шахты ньюбиленда, в других шахтах не проверялся.
!!! Незабудте отменить авто крафт: .options->autoloop<-снять галочку
!!! Если ставите скрипт на крафт кирок, убедитесь что у чара в паке есть инструмент и скилл (45+)
!!! позволяет ему их крафтить, никаких проверок на это я не делал.
Скрипт умеет:
-сам копает
-сам переплавляет руду в инги
-сам куёт из айрона нужное кол-во кирок
-сам куёт нужное кол-во Blacksmithy предметов
-сам же их продаёт указаному вендору
-скидывает Карты в треш-бочку, если таковой нет в радиусе 10 тайлов, скидывает на пол.
-сам реконектится при дисконекте
-сам кушает

Сейчас скрипт настроен на производство бердашей, но вы можете это поправить чуть ниже,НО! будте внимательны к скилу, скрипт начинает ковать с вышки, постепенно спускаясь к айрону.
Если в паке будет вышка, но уровень скила не будет позволять ковать из неё, скрипт зависнет.

}
/////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////    Общие настройки    //////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////

UseBS = 1;                  // В процессе крафтить что то из Blacksmithy? 1-да, 0-нет
UseTinker = 1;              // В процессе крафтить кирки? 1-да, 0-нет
UseSell = 1;				// Если ничего не продаём (UseSell = 0;)
                            // если продаём определённому вендору, вводим его ID (UseSell = $0002B850;)
                            // или ставим 1 (UseSell = 1;), тогда вендор будет искаться в радиусе 20 тайлов
                             
AxeBag = $401835B0;         // Пак с кирками
TypePickAxe = $0E85;        // Type кирки

lang = 'rus';               // Язык на котором будет отображаться информация в стелсовском SystemJournal 'rus' или 'eng'
                            // Если кто нибудь переведёт на инглишь, будет зачемчательно :)

/////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////    Настройки BS    ///////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////

CraftBag = $40170AF0;       // Сумка для ремесла
FindQuantityCraft = 18;     // Кол-во ингов для крафта предмета
CountCraftItem = 150;       // Сколько крафтить предметов за 1 заход 
CountCraftPickAxe = 50;     // Сколько крафтить кирок
XAnvil = 4377;              // X координата возле плавильни
YAnvil = 2775;              // y координата возле плавильни

/////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////    Настройки Mining    /////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////

// 4 угла шахты, X и Y , minX\Y - минимальное значение, maxX\Y - максимальное 
minX = 4376;                // Минимальная координата по оси X
maxX = 4391;                // Максимальная координата по оси X
minY = 2767;                // Минимальная координать по оси Y
maxY = 2773;                // Максимальная координата по оси Y

/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////

var
CraftItem                       : integer;
x9, y9, SellerX, SellerY        : Integer;
t9                              : Word;
Hammer, FoundSeller             : Cardinal;

say1, say2, say3, say4, say5,
say6, say7, say8, say9, say10,
say11, say12, say13, say14, 
say15, say16, say17, say18,
say19, say20, say21, say22,
say23, say24                            : String;



procedure MenuBS;
begin
//////////// что куём? ///////////
    AutoMenu('What','Weapons'); //1
    AutoMenu('What','Polearms');  //2
    AutoMenu('What','Bardiche');  //3  
                                //...? можно добавлять дополнительные строки
//////////////////////////////////
end;

procedure RunTalkMenu (lang : String);
begin
    if lang = 'rus' then
    begin
        say1 := 'Процедура крафта кирок...';
        say2 := 'BlockTinker: Недостаточно ингов для крафта кирок, продолжаем...'; 
        say3 := 'Кирок скрафчено: ';
        say4 := 'ERROR: не могу найти AxeBag, проверьте настройки!';
        say5 := 'ERROR: не могу найти CraftBag, проверьте настройки!'; 
        say6 := 'ERROR: не могу найти инструмент "Hammer" для BSa, проверьте свой backpack!';
        say7 := 'ERROR: не могу найти инструмент "Tinker Tools" для Tinkering, проверьте свой backpack!'; 
        say8 := 'Процедура крафта БС...';           
        say9 := 'БСом скрафчно: ';       
        say10 := 'Определяем type предмета который будем крафтить...';     
        say11 := 'type определён, это:'; 
        say12 := 'ERROR: проверьте настройки координат шахты! "min" не может быть выше "max"!';
        say13 := 'Нет соединения с сервером...';   
        say14 := 'Соединение с сервером установлено!';
        say15 := 'Чар мёртв...';
        say16 := 'Закончились Кирки!';   
        say17 := 'В процессе Mining...';
        say18 := 'openpack.inc: Не могу открыть контейнер! с ним что то не так!';  
        say19 := 'Нет еды';
        say20 := 'ID вендора известен'; 
        say21 := 'ID вендора не известен, ищем вендора...';
        say22 := 'Вендор с которым торгуем: ';
        say23 := 'Вендор не найден, укажите ID вендора в настройках';
        say24 := 'Не могу найти вендора, проверьте настройки "UseSell"';  
    end;
    if lang = 'eng' then
    begin  
        say1 := 'Pickaxe crafting procedure...';
        say2 := 'BlockTinker: Not enough Ings to craft pickaxe, continue...'; 
        say3 := 'Pickaxe made: ';
        say4 := 'ERROR: not found AxeBag, check settings!';
        say5 := 'ERROR: not found CraftBag, check settings!'; 
        say6 := 'ERROR: not found tool "Hammer" for BS, check self backpack!';
        say7 := 'ERROR: not found tool "Tinker Tools" for Tinkering, check self backpack!'; 
        say8 := 'BS crafting procedure...';           
        say9 := 'BS crafting: ';       
        say10 := 'Determine the type of item that we will craft ...';     
        say11 := 'type is defined, it is: '; 
        say12 := 'ERROR: check coordinate settings! "min" cannot be higher than "max"! ';
        say13 := 'No connection to server...';   
        say14 := 'Server connection established!';
        say15 := 'Char is dead...';
        say16 := 'Out of pickaxe!';   
        say17 := 'Mining in processed...';
        say18 := 'openpack: Cant open container! something is wrong with him!';  
        say19 := 'Not food!...';  
        say20 := 'Vendor ID is known'; 
        say21 := 'Vendor ID is known, looking for a vendor...';
        say22 := 'Vendor we trade with: ';
        say23 := 'Vendor not found, specify vendor ID in settings';
        say24 := 'I cant find the vendor, check the "UseSell" settings';
    end;    
end;

procedure CheckSave; 
var Time : TDateTime; 
begin 
Time:= Now - (0.5 / 1440); 
if InJournalBetweenTimes('Saving World State.',Time,Now) >= 0 then Wait(30000); 
end;

procedure OpenPack(OpenItem : Cardinal);
var
t : integer;
begin
    if dead and not Connected then exit;
    if TargetPresent then CancelTarget;
    if (LastContainer <> OpenItem) and (dead = false) then
    begin 
        t := 0;
        repeat
            t := t + 1;
            if not Connected then exit;
            CheckLag(5000);
            Checksave;
            UseObject(OpenItem);
            wait(1000);
        until (LastContainer = OpenItem) or (t >= 15);
        if t >= 15 then AddToSystemJournal(say18); 
    end;
end;

procedure Hungry (food : integer; place : cardinal);
// food: Type еды, place: ID контейнера где искать, при place := 0 и ниже, ищем в своём backpack
var
mTime : TDateTime;
tTimer : integer;
begin
    if place <= 0 then place := backpack;
    if food <= 0 then food := $097B;
    tTimer := 0;
    if TargetPresent then CancelTarget;
    repeat
        tTimer := tTimer + 1;
        FindTypeEx(food,$ffff,place,false);
        if FindCount <= 0 then 
        begin
            AddToSystemJournal(Say19);
            exit;
        end;
        mTime := Now;
        UOSay('.hungry');
        wait(1000);    
        if not (InJournalBetweenTimes('stuffed!', mTime, Now) <> -1) then
        begin
            UseObject(finditem);
            wait(1000);
        end;
    until (InJournalBetweenTimes('stuffed!', mTime, Now) <> -1) or (tTimer >= 5);
end;

function CheckTile (x, y : Word) : Word;
var
a : TStaticCell;
TileType : array [0..24] of Integer;
i, i0 : Integer;
begin
    if not Connected or not CheckLag(60000) or dead then exit;
    TileType[0] := 1339; //типа тайлов шахты
    TileType[1] := 1340;
    TileType[2] := 1341;
    TileType[3] := 1342;
    TileType[4] := 1343;
    TileType[5] := 1344;
    TileType[6] := 1345;
    TileType[7] := 1346;
    TileType[8] := 1347;
    TileType[9] := 1348;
    TileType[10] := 1349;
    TileType[11] := 1350;
    TileType[12] := 1351;
    TileType[13] := 1352;
    TileType[14] := 1353;
    TileType[15] := 1354;
    TileType[16] := 1355;
    TileType[17] := 1356;
    TileType[18] := 1357;
    TileType[19] := 1358;
    TileType[20] := 1359;
    TileType[21] := 1386;
    TileType[22] := 1361;
    TileType[23] := 1362;
    TileType[24] := 1363;
    a := ReadStaticsXY(x, y, WorldNum);
    if a.StaticCount > 0 then
    begin
        for i := 0 to a.StaticCount - 1 do
        begin
            for i0 := 0 to 24 do
            begin
                if a.Statics[i].Tile = TileType[i0] then
                begin
                    result := TileType[i0];
                    exit;
                end;            
            end;   
        end;
    end;
    result := 0;
end;

function CheckPickAxe : Boolean;
begin
    result := False;    
    IgnoreReset;
    if not Connected or not CheckLag(60000) or dead then exit;
    if GetType(ObjAtLayer(RhandLayer)) <> TypePickAxe then
    begin
        if (GetType(ObjAtLayer(RhandLayer)) > 0) then Disarm;
        FindTypeEx(TypePickAxe, $FFFF, backpack, True);
        if FindCount > 0 then
        begin           
            CheckLag(60000); 
            CheckSave;
            Equip(RhandLayer,FindItem);
            wait(500);
        end;
        if FindCount <= 0 then exit;                                                                                                                                                                                                                                                                             
    end;
    result := True;
end;

procedure Mining (tile, x, y, z : Integer);
var
ctime : TDateTime;
txtR, txtF : String;
t : Integer;
begin 
    txtF := 'There is no metal here to mine.';
    txtR := 'You stop mining.';
    repeat    
        if not Connected or not CheckLag(60000) or dead then exit;    
        if not CheckPickAxe then exit;
        t := 0;
        if TargetPresent then CancelTarget;
        ctime := Now;
        CheckLag(60000);
        UseObject(ObjAtLayerEx(RhandLayer,self));
        WaitForTarget(5000);
        if TargetPresent then TargetToTile(tile, x, y, z);
        repeat
            t := t + 1; 
            if GetType(ObjAtLayer(RhandLayer)) <> TypePickAxe then t := 150;    
            if not Connected or dead then exit;
            CheckSave;
            wait(100);
        until (InJournalBetweenTimes(txtR, ctime, Now) <> -1) or (t >= 150);
    until (InJournalBetweenTimes(txtF, ctime, Now) <> -1) or (t >= 150);
end;

procedure ProcessedResource;
var
ctime : TDateTime;
t : integer;
begin
    if not Connected or not CheckLag(60000) or dead then exit; 
    while (XAnvil <> GetX(Self)) or (YAnvil <> GetY(Self)) do
    begin 
        if not Connected or not CheckLag(60000) or dead then exit;
        CheckSave;
        CheckLag(60000);
        newMoveXY(XAnvil, YAnvil, True, 0, False); 
    end;
    repeat         
        FindTypeEx($19B8, $FFFF, Backpack,False); 
        if FindCount > 0 then
        begin  
            repeat   
                if not Connected or not CheckLag(60000) or dead then exit;
                if TargetPresent then CancelTarget;
                ctime := Now;
                CheckSave;
                CheckLag(60000);
                UseObject(finditem);
                t := 0;
                repeat  
                    t := t + 1; 
                    wait(100);
                    if not Connected or not CheckLag(60000) or dead then exit;
                    CheckSave;
                    CheckLag(60000);
                until (InJournalBetweenTimes('Success:|Failed.', ctime, Now) <> -1) or (t >= 300);
            until (InJournalBetweenTimes('Success:|Failed.', ctime, Now) <> -1) or (t >= 300);
        end;
    until FindCount <= 0; 
end;

function CreatePickAxe : Boolean;
var
t : integer;
tm : TDateTime;
begin
    result := false; 
    IgnoreReset;
    if not Connected or not CheckLag(60000) or dead then exit;
    if TargetPresent then CancelTarget;
    if MenuPresent then CloseMenu;
    if TargetPresent then CancelTarget;
    if MenuHookPresent then CancelMenu();
    AddToSystemJournal(say1);
    FindTypeEx($1BF2, $0000, Backpack, False);
    if FindQuantity < 4 then exit;
    repeat   
        if not Connected or not CheckLag(60000) or dead then exit;  
        CheckLag(60000);
        CheckSave;
        UseType($1EBC, $FFFF);
        WaitForTarget(5000);
    until TargetPresent;
    AutoMenu('What','Deadly Tools');
    AutoMenu('What','Pickaxe');
    WaitTargetObject(FindItem);
    tm := Now;
    t := 0;
    CheckSave;
    repeat     
        if not Connected or not CheckLag(60000) or dead then exit;
        wait(100);
        t := t + 1;
    until (InJournalBetweenTimes('skilled enough|Success|You destroyed|You stop.|You are already holding the item', tm, Now) <> -1) or (t >= 150);
    wait(500);
    result := True;    
end;

function Sepor (GID : Cardinal) : String;
var
SL : TStringList;
begin
    SL := TStringList.Create;
    StrBreakApart(GetTooltip(GID), '|', SL);
    result := SL.Strings[SL.Count - 1];
    SL.Free;
end;

function CheckAllRecouce : Boolean;
var
a, c, h, ids, tt, i : integer;
List: TStringList;
ItemIDList : Cardinal;
begin 
    if not Connected or not CheckLag(60000) or dead then exit;
    checksave;
    Disarm;
    wait(1000);
    result := False; 
    FindTypeEx($FFFF, $FFFF, Backpack, False);
    List := TStringList.Create; 
    if GetFindedList(List) then 
        for i := 0 to List.Count - 1 do
        begin 
            ItemIDList := StrToInt('$'+List.Strings[i]);
            if ItemIDList = AxeBag then a := 1; 
            if (ItemIDList = CraftBag) or (UseBS = 0) then c := 1;  
            if (GetType(ItemIDList) = $1EBC) or (UseTinker = 0) then tt := 1;
            if (GetType(Hammer) <> $13E3) and (GetType(ItemIDList) = $13E3) and (UseTinker = 1) then 
            begin
                h := 1;
                Hammer := ItemIDList;
            end;
        end;
    List.Free;   
    
    if UseSell <= 0 then ids := 1;
    if UseSell > 1 then
    begin  
        AddToSystemJournal(say20);
        FoundSeller := UseSell;
        if GetType(FoundSeller) <> $0191 then 
        begin
            ids := 0
            AddToSystemJournal(say24);
        end
        else
        begin     
            AddToSystemJournal(say22+GetName(FoundSeller)); 
            SellerX := GetX(FoundSeller);
            SellerY := GetY(FoundSeller);
            ids := 1;
        end;    
    end;
    if UseSell = 1 then
    begin
    AddToSystemJournal(say21);
    FindTypeEx($FFFF, $FFFF, Ground, False);
    List := TStringList.Create; 
    if GetFindedList(List) then
    begin 
        ids := 0;
        for i := 0 to List.Count - 1 do
        begin 
            ItemIDList := StrToInt('$'+List.Strings[i]);
            if Sepor(ItemIDList) = 'Invulnerable' then 
            begin 
                FoundSeller := ItemIDList;
                ids := 1  
                AddToSystemJournal(say22+GetName(FoundSeller));
                break;
            end;
        end;
        if ids = 0 then AddToSystemJournal(say23);
    end; 
    List.Free;
    end;
    if a = 0 then AddToSystemJournal(say4);
    if c = 0 then AddToSystemJournal(say5); 
    if h = 0 then AddToSystemJournal(say6);
    if tt = 0 then AddToSystemJournal(say7);
    if a + c + h + tt + ids >= 5 then result := True;
end;

procedure BS (ing : cardinal);
var
t : integer;
tm : TDateTime;
begin
    if not Connected or not CheckLag(60000) or dead then exit;
    if MenuPresent then CloseMenu;
    if TargetPresent then CancelTarget;
    if MenuHookPresent then CancelMenu();
    while (XAnvil <> GetX(Self)) or (YAnvil <> GetY(Self)) do
    begin 
        if not Connected or not CheckLag(60000) or dead then exit;
        CheckSave;
        CheckLag(60000);
        newMoveXY(XAnvil, YAnvil, True, 0, False); 
    end;
    AddToSystemJournal(say8);
    if (GetType(ObjAtLayer(RhandLayer)) <> $13E3) then
    begin
        if ObjAtLayer(RhandLayer) > 0 then
        begin
            Disarm;
            wait(1000);
        end;           
        CheckLag(60000); 
        CheckSave;
        Equip(RhandLayer,Hammer);
        wait(1000);                                                                                                                                                                                                                                                                         
    end;
    MenuBS;
    repeat        
        if not Connected or not CheckLag(60000) or dead then exit;
        CheckSave;
        UseObject(Hammer);
        WaitForTarget(5000);
    until TargetPresent;
    WaitTargetObject(ing);
    tm := Now;
    t := 0;
    repeat             
        if not Connected or dead then exit;
        CheckSave;
        wait(100);
        t := t + 1;
    until (InJournalBetweenTimes('Success|You destroyed|You stop.', tm, Now) <> -1) or (t >= 150);
    wait(500);    
end;

procedure Stuck(color : Word);
var
s1 : Cardinal;
List: TStringList;
i: Integer;
begin
    repeat    
        if not Connected or dead then exit;
        if TargetPresent then CancelTarget;
        FindTypeEx($1BF2, color, Backpack, False);
        List := TStringList.Create; 
        if GetFindedList(List) then 
            for i := 0 to List.Count - 1 do
                if GetQuantity(StrToInt('$'+List.Strings[i])) >= 60000 then Ignore(StrToInt('$'+List.Strings[i]));
        List.Free;     
        FindTypeEx($1BF2, color, Backpack, False);     
        if FindCount <= 1 then exit;
        s1 := FindItem; 
        Ignore(finditem);
        FindTypeEx($1BF2, color, Backpack, False);
        if FindCount >= 1 then
        begin   
            if TargetPresent then CancelTarget;
            MoveItem(s1, 60000 - FindQuantity, finditem, 0,0,0); 
            wait(900);
            IgnoreReset;
        end;  
    until FindCount <= 1;
    IgnoreReset;
end;

function ChoseIngos : cardinal;
var
i : integer;
ing : array of Cardinal;
SkillBS : array of Cardinal;
begin
    if not Connected or not CheckLag(60000) or dead then exit;
    if Length(SkillBS) < 1 then
    begin
        SetLength(SkillBS, 10);
        SetLength(ing, 10);
    end;
    ing[9] := $0000;    // iron
    ing[8] := $0602;    // copper
    ing[7] := $0425;    // bd
    ing[6] := $050C;    // pagan
    ing[5] := $03E9;    // silver
    ing[4] := $0483;    // spectral  
    ing[3] := $054E;    // lava
    ing[2] := $04E7;    // icerock 
    ing[1] := $07EC;    // mythril
    ing[0] := $0487;    // basilisk
    SkillBS [9] := 0;
    SkillBS [8] := 60;
    SkillBS [7] := 70;
    SkillBS [6] := 80;
    SkillBS [5] := 90;
    SkillBS [4] := 100;
    SkillBS [3] := 105;
    SkillBS [2] := 110;
    SkillBS [1] := 115;
    SkillBS [0] := 117;
    for i := 0 to High(ing) do
    begin 
        if dead or not Connected then exit;
        Stuck(ing[i]);
    end;
    for i := 0 to High(SkillBS) do
    begin 
        if not Connected or dead then exit;
        if GetSkillValue('Blacksmithy') >= SkillBS[i] then
        begin
            repeat  
                if not Connected or dead then exit;
                FindTypeEx($1BF2, ing[i], Backpack, False);
                if FindQuantity < FindQuantityCraft then Ignore(FindItem);
            until (FindQuantity >= FindQuantityCraft) or (FindCount <= 0);
            if FindCount > 0 then 
            begin
                result := FindItem;
                IgnoreReset;
                exit;  
            end;
            result := 0;
        end;
    end;  
end;

procedure GetTypeCraftItem;
var
craft : cardinal;
SL : TStringList;
begin
    if not Connected or not CheckLag(60000) or dead then exit;
    AddToSystemJournal(say10); 
    craft := ChoseIngos; 
    if craft <= 0 then exit;
    repeat  
        if not Connected or dead then exit;             
        FindTypeEx($FFFF, $FFFF, Backpack, True);
        if FindCount > 0 then Ignore(FindItem);
    until FindCount <= 0;
    repeat
        BS(craft); 
        if not Connected or dead then exit;
        FindTypeEx($FFFF, $FFFF, Backpack, True);
    until FindCount > 0;
    CheckLag(60000);
    CheckSave;
    repeat  
        if not Connected or dead then exit;             
        FindTypeEx($1BF2, $FFFF, Backpack, True);
        if FindCount > 0 then Ignore(FindItem);
    until FindCount <= 0;       
    FindTypeEx($FFFF, $FFFF, Backpack, True); 
    if FindCount > 0 then CraftItem := GetType(FindItem);
    if TargetPresent then CancelTarget;
    MoveItems(Backpack, CraftItem, $FFFF, CraftBag, 1,1,1, 300);
    IgnoreReset;   
    
    SL := TStringList.Create;
    StrBreakApart(GetTooltip(finditem), '|', SL);
    AddToSystemJournal(say11+SL.Strings[0]);
    SL.Free;
end; 

function Sell : Boolean;
var
t : integer;
tm : TDateTime;
begin
    IgnoreReset;
    result := False; 
    if TargetPresent then CancelTarget;
    if (GetX(FoundSeller) <= 0) or (GetY(FoundSeller) <= 0) then
    begin 
        if not Connected or not CheckLag(60000) or dead then exit;
        CheckSave;
        CheckLag(60000);
        newMoveXY(SellerX, SellerY, True, 0, False);
        wait(1000);
        if (GetX(FoundSeller) <= 0) or (GetY(FoundSeller) <= 0) then exit;  
    end;
    while (GetX(Self) <> GetX(FoundSeller)) or (GetY(Self) <> GetY(FoundSeller)) do
    begin 
        if not Connected or not CheckLag(60000) or dead then exit;
        CheckSave;
        CheckLag(60000);
        newMoveXY(GetX(FoundSeller), GetY(FoundSeller), True, 0, False);
        wait(1000);   
    end;
    t := 0;
    UOSay('Sell Bag');
    repeat   
        if not Connected or not CheckLag(60000) or dead then exit;
        CheckSave;
        CheckLag(60000);
        t := t + 1;
        wait(100);
    until TargetPresent or (t >= 150);
    if not TargetPresent or (t >= 150) then exit; 
    t := 0;      
    tm := Now;
    WaitTargetObject(CraftBag);
    repeat  
        if not Connected or not CheckLag(60000) or dead then exit;
        CheckSave;
        wait(100);
        t := t + 1;
    until (InJournalBetweenTimes('Your sale total is', tm, Now) <> -1) or (t >= 350);
    result := True;   
end;

procedure BlockTinker;
begin
    if not Connected or not CheckLag(60000) or dead then exit;
    FindTypeEx(TypePickAxe, $FFFF, Backpack, false);
    if FindCount > 0 then MoveItems(Backpack, TypePickAxe, $FFFF, AxeBag, 1,1,1, 300);
    repeat 
        FindTypeEx(TypePickAxe, $FFFF, AxeBag, false);
        if FindCount < CountCraftPickAxe then 
        begin
            if not dead and Connected then
            begin
                if not CreatePickAxe then 
                begin    
                    FindTypeEx($1BF2, $0000, Backpack, False);
                    if FindQuantity < 4 then
                    begin
                        AddToSystemJournal(say2);
                        exit;
                    end;
                end;     
                if TargetPresent then CancelTarget;     
                FindTypeEx(TypePickAxe, $FFFF, Backpack, false);
                if FindCount > 0 then 
                begin
                    MoveItems(Backpack, TypePickAxe, $FFFF, AxeBag, 1,1,1, 300);
                    FindTypeEx(TypePickAxe, $FFFF, AxeBag, false); 
                    AddToSystemJournal(say3+IntToStr(FindCount)+'/'+IntToStr(CountCraftPickAxe));       
                end;
            end;
        end;
        if not Connected or not CheckLag(60000) or dead then exit;
    until FindCount >= CountCraftPickAxe;
end;

procedure BlockBS;
var 
ingots : cardinal;
begin
    repeat        
        if not Connected or dead then exit;   
        IgnoreReset;
        if CraftItem <= 0 then GetTypeCraftItem;
        if CraftItem <= 0 then exit;
        FindTypeEx(CraftItem, $FFFF, Backpack, false);
        if FindCount > 0 then MoveItems(Backpack, CraftItem, $FFFF, CraftBag, 1,1,1, 300);
        FindTypeEx(CraftItem, $FFFF, CraftBag, false);
        if FindCount < CountCraftItem then
        begin
            ingots := ChoseIngos;
            if ingots <= 0 then exit;           
            if ingots > 0 then BS(ingots);
            FindTypeEx(CraftItem, $FFFF, backpack, false);        
            if FindCount > 0 then
            begin
                MoveItems(Backpack, CraftItem, $FFFF, CraftBag, 1,1,1, 300);
                FindTypeEx(CraftItem, $FFFF, CraftBag, false); 
                AddToSystemJournal(say9+IntToStr(FindCount)+'/'+IntToStr(CountCraftItem));
            end;
        end
        else
        begin
            repeat
                wait(100);
                if not Connected or dead then exit;
            until Sell;
        end;
    until ingots <= 0;
end;

procedure DropMaps;
var
MovePlace : Cardinal;
begin
    if not Connected or not CheckLag(60000) or dead then exit;
    FindTypeEx($14ED, $FFFF, backpack, False);
    if FindCount <= 0 then exit;
    FindTypeEx($0E77, $FFFF, Ground, False);
    if FindCount > 0 then 
    begin
        newMoveXY(GetX(finditem), GetY(finditem), True, 1, False);
        MovePlace := finditem;
    end
    else
    begin
        MovePlace := Ground;
    end;   
    if TargetPresent then CancelTarget;
    MoveItems(Backpack, $14ED, $FFFF, MovePlace, 1,1,1, 300);
end;

procedure RunConnect;
begin
    if not Connected then
    begin 
        AddToSystemJournal(say13);
        repeat          
            while not Connected do
            begin    
                Connect;
                wait(5000);
                if Connected and not CheckLag(5000) then 
                begin
                    Disconnect;
                    wait(3000);
                end;
            end;
        until Connected;
        if Connected then AddToSystemJournal(say14);
    end;
    OpenPack(Backpack);
    OpenPack(AxeBag);
    OpenPack(CraftBag);
end;

begin
    FindDistance := 20;
    MoveCheckStamina := 0;
    MoveThroughNPC := 0;
    RunTalkMenu(lang);
    if (minX > maxX) or (minY > maxY) then
    begin
        AddToSystemJournal(say12);
        exit;
    end;
    RunConnect;
    CancelMenu;
    if not CheckAllRecouce then exit;
    ProcessedResource;
    BlockTinker;
    BlockBS;
    DropMaps;
    repeat
        for y9 := minY to maxY do
        begin
            for x9 := minX to maxX do
            begin
                if not Connected then RunConnect;
                t9 := CheckTile(x9, y9);
                if t9 > 0 then
                begin
                    while dead or not Connected do
                    begin      
                        RunConnect;
                        if dead and Connected then
                        begin    
                            AddToSystemJournal(say15);
                            exit;
                        end;  
                        IgnoreReset;
                        ProcessedResource;
                        BlockTinker;
                        BlockBS; 
                        DropMaps;
                    end;
                    if not CheckPickAxe then
                    begin
                        if UseTinker = 0 then 
                        begin
                            AddToSystemJournal(say16);
                            SetARStatus(False);
                            Disconnect;
                            Exit;
                        end;
                        if UseBS+UseTinker >= 1 then
                        begin
                            ProcessedResource;
                            BlockTinker;
                            BlockBS;  
                        end; 
                        if UseSell = 1 then 
                            if not Sell then exit; 
                        DropMaps;
                    end;
                    if newMoveXY(x9, y9, True, 2, False) then
                    begin 
                        hungry(0,0);
                        AddToSystemJournal(say17+' ('+IntToStr(x9)+'), ('+IntToStr(y9)+')');
                        Mining(t9, x9, y9, GetZ(Self));  
                    end;
                end;
            end;
        end;     
    until False;
end.