Program Mining_Cove;
const
////////////////////////////////////////////////////////////////////////////
// 4 угла шахты, X и Y , minX\Y - минимальное значение, maxX\Y - максимальное 
minX = 2186;    //Минимальная координата по оси X
maxX = 2209;    //Максимальная координата по оси X
minY = 1153;    //Минимальная координать по оси Y
maxY = 1246;    //Максимальная координата по оси Y
////////////////////////////////////////////////////////////////////////////
ForgeX = 2219;  //Координаты X плавильни
ForgeY = 1164;  //Координаты Y плавильни
//
BankX = 2229;   //Координаты X банка
BankY = 1193;   //Координаты Y банка
//
                //Координаты и тип тайла воды, где будем мыть бинты (,infotile)
WaterX = 2228;  //X    
WaterY = 1140;  //Y
WaterZ = -1;    //Z
WaterT = 6042;  //Tile
////////////////////////////////////////////////////////////////////////////
BagID = $40053CF5;  //ID контейнера в банке, куда скидывать остальные предметы в паке
CountBandage = 100; //Кол-во бандажек которые берём с собой
CountTools = 2;     //Кол-во инструмента котогоро берём с собой   
PrimAb = 1;         //Использование PrimaryAbility 1-да, 0-нет  
//Type вещей которых не надо убирать в банк
//IgnoreResource = '$2252,$0E21,$0E22,$0E86,$1769';
//$2252; //книга
//$0E21; //бинты
//$0E22; //грязные бинты
//$0E86; //кирка
//$1769; //ольцо с ключем

var
x9, y9 : Integer;
t9 : Word;
bank : Cardinal;


function CheckTile (x, y : Word) : Word;
var
a : TStaticCell;
TileType : array [0..24] of Integer;
i, i0 : Integer;
begin
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

procedure CheckBloodBandage;
var 
t : Integer;
ctime : TDateTime;
begin
    FindTypeEx($0E22, $FFFF, Backpack, False);
    while FindCount > 0 do
    begin
        if not newMoveXY(WaterX, WaterY, True, 2, True) then
        begin
            AddToSystemJournal('ERROR: Не могу добраться до воды!');  
            exit;
        end;  
        ctime := Now;
        if TargetPresent then CancelTarget;
        if Dead then exit;
        CheckLag(60000);
        UseObject(FindItem);
        t := 0; 
        repeat           
            t := t + 1;
            wait(100);
        until (InJournalBetweenTimes('Who will you use the bandages on?', ctime, Now) <> -1) or (t >= 50);
        WaitTargetTile(WaterT, WaterX, WaterY, WaterZ);
        wait(1000);
        FindTypeEx($0E22, $FFFF, Backpack, False);
    end;
end;


procedure ProcesedResource;
var
TypeResouce : array [0..1] of Word;
i : Integer;
begin
    if Dead then exit;
    TypeResouce[0] := $19B9;
    TypeResouce[1] := $19B8; 
    for i := 0 to High(TypeResouce) do
    begin  
        FindTypeEx(TypeResouce[i], $FFFF, Backpack, True); 
        if FindCount > 0 then
        begin 
            // идём к плавильнe
            while (GetX(Self) <> ForgeX) and (GetY(Self) <> ForgeY) do
            begin
                if Dead then exit;
                if not newMoveXY(ForgeX, ForgeY, True, 0, True) then wait(1000);
            end;    
            // перерабатываем всё что есть  
            repeat 
                FindTypeEx(TypeResouce[i], $FFFF, Backpack, True); 
                if FindCount > 0 then 
                begin
                    if Dead then exit;
                    CheckLag(60000);
                    UseObject(FindItem);
                    wait(300);
                end;
            until FindCount <= 0;
        end;
    end;
    CheckBloodBandage;
end;

procedure GoToBank;
var
t : Integer;
begin
    while (GetX(Self) <> BankX) and (GetY(Self) <> BankY) do
    begin
        if Dead then exit;
        if not newMoveXY(BankX, BankY, True, 0, True) then wait(1000);
    end;
    repeat  
        if Dead then exit;
        UOSay('bank');
        t := 0;
        repeat
            wait(100);
        until (LastContainer = ObjAtLayer(BankLayer)) or (t >= 50);
    until (LastContainer = ObjAtLayer(BankLayer));
    bank := ObjAtLayer(BankLayer);
    repeat
        if Dead then exit;
        CheckLag(60000);
        UseObject(BagID);
        t := 0;
        repeat
            wait(100);
        until (LastContainer = BagID) or (t = 50);
    until (LastContainer = BagID);
end;

procedure DropResource;
var
TypeResouce : array [0..1] of Word;
SelfResource : array of Word;
 i : Integer;
begin
// идём к банку 
    GoToBank;
    TypeResouce[0] := $1BF2; //инги
    TypeResouce[1] := $11EA; //вата
    for i := 0 to 1 do
    begin
        repeat 
            if Dead then exit;
            FindTypeEx(TypeResouce[i], $FFFF, Backpack, False);
            if FindCount > 0 then 
            begin
                CheckLag(60000);
                MoveItem(FindItem, FindQuantity, bank, 0,0,0);
                wait(900);    
            end;
        until FindCount <= 0;
    end;
    SelfResource := [$2252,$0E21,$0E22,$0E86,$1769];
    for i := 0 to High(SelfResource) do
    begin 
        repeat
            FindTypeEx(SelfResource[i], $FFFF, Backpack, False);
            if FindCount > 0 then
                Ignore(FindItem);
        until FindCount <= 0;    
    end; 
    repeat
        if Dead then exit;
        FindTypeEx($FFFF, $FFFF, Backpack, False);
        if FindCount > 0 then
        begin 
            CheckLag(60000);
            MoveItem(FindItem, FindQuantity, BagID, 0,0,0);
            wait(900);
        end;
    until FindCount <= 0; 
    IgnoreReset;  
end;

function GetResource : Boolean;
var
TypeResouce : array [0..1] of Word;
ResaontExit : array [0..1] of String;
i, c : Integer;
begin
    result := False;
    GoToBank; 
    if Dead then exit;
//проверяем кол-во ресов в банке, если чего-то нет, выходим
    TypeResouce[0] := $0E21; //бинты
    TypeResouce[1] := $0E86; //кирка 
    ResaontExit[0] := 'ERROR: В банке нет бинтов!';
    ResaontExit[1] := 'ERROR: В банке нет инструмента'; 
    for i := 0 to 1 do
    begin
        FindTypeEx(TypeResouce[i], $FFFF, bank, False); 
        if FindCount <= 0 then 
        begin
            AddToSystemJournal(ResaontExit[i]);
            exit;                              
        end;
    end; 
//пополняем запас бинтов
    FindTypeEx(TypeResouce[0], $FFFF, Backpack, False);
    c := FindFullQuantity;
    if c < CountBandage then
    begin
        FindTypeEx(TypeResouce[0], $FFFF, bank, False);   
        if FindCount > 0 then
        begin  
            CheckLag(60000);
            MoveItem(FindItem, CountBandage-c, Backpack, 0,0,0);
            wait(900);
        end; 
    end;
//пополняем запас кирок
    c := 0;
    if (GetType(ObjAtLayer(RhandLayer)) = TypeResouce[1]) then c := 1;
    FindTypeEx(TypeResouce[1], $FFFF, Backpack, False);
    c := c + FindCount;
    while c < CountTools do
    begin
        FindTypeEx(TypeResouce[1], $FFFF, bank, False); 
        if FindCount > 0 then 
        begin  
            CheckLag(60000);
            MoveItem(FindItem, 1, Backpack, 0,0,0);
            wait(900);
            c := c + 1;
        end;
    end;
    result := True;
end;

function CheckPickaxe : Boolean;
var
c : Integer;
begin
    result := False;
    c := 0;
    if (GetType(ObjAtLayer(RhandLayer)) = $0E86) then c := 1;
    FindTypeEx($0E86, $FFFF, Backpack, False);
    c := c + FindCount;
    if c > 0 then result := True;
end;

function CheckAll : Boolean;
begin
    result := False;
    if (Weight >= MaxWeight - 50) or not CheckPickaxe then
    begin 
        ProcesedResource;
        DropResource;
        if not GetResource then exit;
    end;
    result := True;
end;

procedure Heal;
var
buffObj : TBuffBarInfo;
buffIconObj : TBuffIcon;
i, t : integer;
ctime : TDateTime;
begin
    if dead then exit;
    if GetHP(Self) < GetMaxHP(Self) then
    begin
        ctime := Now;
        buffObj := GetBuffBarInfo();
        t := 0;
        if (buffObj.Count > 0) then
        begin
            for i := 0 to buffObj.Count - 1 do
            begin
                buffIconObj := buffObj.Buffs[i];
                if (buffIconObj.Attribute_ID = $0000042D) then t := 1;
            end;
        end;
        if t <= 0 then
        begin
            UOSay('[BandSelf');
            repeat
                t := t + 1;
                wait(100);
            until (InJournalBetweenTimes('You begin applying the bandages', ctime, Now) <> -1) or (t >= 10);
        end;
    end;
end;

procedure ActivAbility;
var a : Integer;
begin
    a := StrToInt(GetActiveAbility);
    if a < 0 then UsePrimaryAbility;
end;

procedure CheckWar;
var
TypeGolem : array of Word;
Golem, GolemCorps : Cardinal;
x, y, i : Integer;
begin
    if not WarMode then SetWarMode(True);
    TypeGolem := [$006E,$00A6,$006F,$006B,$006C,$0071,$006D];
    Heal;
    for i := 0 to High(TypeGolem) do
    begin
        repeat 
            FindTypeEx(TypeGolem[i], $FFFF, Ground, False);
            if FindCount > 0 then
            begin    
                Golem := FindItem;
                Attack(Golem);
                repeat 
                    Heal; 
                    if PrimAb = 1 then ActivAbility;
                    x := GetX(Golem);
                    y := GetY(Golem);
                    if GetDistance(Golem) > 1 then newMoveXY(x, y, True, 1, True);
                    if GetHP(Self) <= 50 then
                    begin
                        newMoveXY(2239, 1195, True, 0, True);
                        while HP <= Round((MaxHP/100)*80) do
                        begin     
                            Heal;
                            wait(100);
                        end;
                        if (GetX(Self) <> x) and (GetY(Self) <> y) then newMoveXY(x, y, True, 0, True);
                    end;   
                until (GetHP(Golem) <= 0); 
                FindTypeEx(TypeGolem[i], $0000, Ground, True);
            end;
        until FindCount <= 0;
    end;
    repeat
        FindTypeEx($2006, $FFFF, Ground, False);
        if FindCount > 0 then
        begin
            newMoveXY(GetX(FindItem), GetY(FindItem), True, 1, True);
            repeat
                CheckLag(60000);
                UseObject(FindItem);
                wait(1000);
            until LastContainer = FindItem;
            GolemCorps := FindItem;
            repeat 
                FindTypeEx($FFFF, $FFFF, GolemCorps, False);
                if FindCount > 0 then
                begin
                    CheckLag(60000);
                    MoveItem(FindItem, FindQuantity, Backpack, 0,0,0);
                    wait(900);
                end;
            until FindCount <= 0;
            Ignore(GolemCorps);
        end;
    until FindCount <= 0;
end;

function Mining (tile, x, y, z : Integer) : Boolean;
var
ctime : TDateTime;
txtR, txtF : String;
t, w, x0, y0 : Integer;
begin
    result := False;
    txtR := 'You loosen some rocks|in your backpack.|There is no metal here to mine.|You can|pile of ore with which to combine it.|Target cannot be seen.|You have worn out your tool!|That is too far away.|You must wait to perform another action.|away to continue mining.';
    txtF := 'There is no metal here to mine.|You can|pile of ore with which to combine it.|Target cannot be seen.|That is too far away.';
    x0 := GetX(Self);
    y0 := GetY(Self);
    repeat        
        if dead then exit;
        if not CheckAll then exit;
        if (GetType(ObjAtLayer(RhandLayer)) <> $0E86) then
        begin
            FindTypeEx($0E86, $FFFF, Backpack, False);
            if FindCount >= 0 then
            begin           
                CheckLag(60000);
                Equip(RhandLayer,FindItem);
                wait(500);
            end;
        end;
        CheckWar;
        t := 0;
        while ((GetX(Self) <> x0) or (GetY(Self) <> y0)) and (t <= 10) do
        begin                                  
            t := t + 1;    
            newMoveXY(x0, x0, True, 0, True);        
            wait(100);
        end;
        if TargetPresent then CancelTarget;
        ctime := Now;
        CheckLag(60000);
        UseObject(ObjAtLayerEx(RhandLayer,self));
        WaitForTarget(5000);
        if TargetPresent then TargetToTile(tile, x, y, z);
        t := 0;
        repeat 
            wait(100);
            t := t + 1;
            CheckWar;
            w := 0;
            while ((GetX(Self) <> x0) or (GetY(Self) <> y0)) and (w <= 10) do
            begin                                  
                w := w + 1;    
                if not newMoveXY(x0, x0, True, 0, True) then exit;
            end;
        until (InJournalBetweenTimes(txtR, ctime, Now) <> -1) or (t >= 80);
    until (InJournalBetweenTimes(txtF, ctime, Now) <> -1);
    result := True;
end;

procedure test;
begin
    FindDistance := 10; 
    MoveCheckStamina := 0;
    MoveThroughNPC := 5000;
    if (minX > maxX) and (minY > maxY) then
    begin   
        AddToSystemJournal('ERROR: проверьте настройки координат шахты! "min" нет может быть выше "max"!');
        exit;   
    end;
    while not dead do
    begin 
        for y9 := minY to maxY do
        begin
            for x9 := minX to maxX do
            begin
                if not CheckAll then exit;
                t9 := CheckTile(x9, y9);
                if t9 > 0 then
                begin
                    if newMoveXY(x9, y9, True, 2, True) then 
                        Mining(t9, x9, y9, GetZ(Self));
                end;          
            end;
        end;  
    end; 
end;



procedure test1;
begin
    AddToSystemJournal(GetActiveAbility);
end;


begin
    test;
end.
