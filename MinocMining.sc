Program MiningMinoc;
{$Include 'all.inc'}
const
////////////////////////////////////////////////////////////////////////////
// Настройки мининга
Trigger = 5000;             //0 - 64к(максимальный вес) или указать нужный тригер веса, -1 по концу кирок
PickAxeRules = 0;   //ID контейнера с кирками, 0 - делаем кирки из нишки (ВНИМАНИЕ! на первый пункт)
// !!! ^ Если Trigger = -1, а PickAxeRules = 0, то бот будет работать как при Trigger = 0
////////////////////////////////////////////////////////////////////////////

CountPickAxe = 2;          //Кол-во кирок
CountFood = 50;             //сколько еды брать с собой, 0 - не кормин чара
Trash = $52344989;          //Trash бочка дома
////////////////////////////////////////////////////////////////////////////
// Настройка рунбуки // нумерация сверху в низ
RunHome = 1;                //Руна домой    
RunMinoc = 4;               //Руна в минок
////////////////////////////////////////////////////////////////////////////
// 4 угла шахты, X и Y , minX\Y - минимальное значение, maxX\Y - максимальное 
minX = 2560;                //Минимальная координата по оси X
maxX = 2594;                //Максимальная координата по оси X
minY = 445;                 //Минимальная координать по оси Y
maxY = 490;                 //Максимальная координата по оси Y
////////////////////////////////////////////////////////////////////////////
// координаты дома, указываем центр, скрипт определяет радиус в 25 тайлов
// указывать рядом с наковальней и плавильней, не дальше 1 тайла от обоих 
homeX = 1162;               //X Координата в доме
homeY = 2405;               //Y Координата в доме
//Координаты 'X,Y' перед дверьми в доме, начало с улицы
homeDoor = '1163,2416,1165,2408';
//Граници в доме Xmin,Xmax,Ymin,Ymax
homeCoord = '1156,1169,2403,2414';


type
    TileRecord = Record
    t,x,y : integer;
end;
type
    AllTileRecord = Record
    t,x,y : integer;
end;
type
    GetBlock = Record
    x,y : integer;
end;
type
    HDRecord = Record
    x,y : integer;
end;

var
HD : array of HDRecord;
Tile : array of TileRecord;
AllTile : array of AllTileRecord;
Block : array of GetBlock;
HXMax,HXMin,HYMax,HYMin : integer;
Err, lock, i : Integer;


procedure CheckConnect;
var w : Integer;
begin
    w := 0;
    if not Connected then
    begin
        Connect;
        AddToSystemJournal('Нет соединения с сервером. Ждём логина...');
        w := 1; 
    end; 
    repeat 
        wait(100);
    until Connected;
    if w = 1 then AddToSystemJournal('Зашли, продолжаем.'); 
    if LastContainer <> Backpack then
    begin
        CheckLag(50000);
        UseObject(Backpack);
        wait(500);
    end;
end;

function CheckDead : Boolean;
begin
    CheckConnect;
    if dead then
    begin
        AddToSystemJournal('Чар мёртв!');
        Err := 1;
        result := True;
    end;               
    result := False;
end;

function CheckWeight : Boolean;
begin
    CheckConnect;
    if Weight < MaxWeight then Result := True; 
    if Weight >= MaxWeight then Result := False;
end;

function CheckLock : Integer; //0 Дом, 1 Минок, 2 Шахта минока,  -1 error
begin
    CheckConnect;
    result := -1;
    if (GetX(Self) <= homeX + 25) and (GetX(Self) >= homeX - 25) and (GetY(Self) <= homeY + 25) and (GetY(Self) >= homeY - 25) then result := 0;
    
    if ((GetY(Self) > 499) and (GetY(Self) <= 499+300)) or ((GetY(Self) < 445) and (GetY(Self) >= 445-300))
        or ((GetX(Self) > 2594) and (GetX(Self) <= 2594+300)) or (GetX(Self) < 2557) and (GetX(Self) >= 2557-300) then result := 1;  
        
        
    if (GetX(Self) <= 2594) and (GetY(Self) <= 499) and (GetX(Self) >= 2557) and (GetY(Self) >= 445) then result := 2;
    if result < 0 then 
    begin
        AddToSystemJournal('Error: Не могу определить свои координаты!');
        Err := 1;
    end;
end;

function Recharge : Boolean;
var
SL : TStringList;
SF, SC : Integer;
txt : String;
SB : Cardinal;
begin
    CheckConnect;
    result := False;
    Err := 1;
    if CheckDead then exit;
    FindTypeEx($0EFA, $021E, Backpack, False);
    if FindCount = 1 then
    begin
        SB := FindItem;
        SL := TStringList.Create;
        StrBreakApart(GetCliloc(SB), ':', SL);
        if SL.Count > 0 then
        begin
            txt := SL.Strings[SL.Count - 1];
            txt := stringreplace(txt,' ','',[rfReplaceAll]);
            SL.Free;
            SL := TStringList.Create;
            StrBreakApart(txt, '/', SL); 
            SC := StrToInt(SL.Strings[0]);
            SF := StrToInt(SL.Strings[1]);
        end;
        SL.Free;
        if SC < 2 then
        begin
            FindTypeEx($1F4C, $0000, Ground, False);
            if (FindCount <= 0) and (FindQuantity < 2) then
            begin
                AddToSystemJournal('Error: На земле меньше 2 Rercall скролов! ('+IntToStr(FindQuantity)+')');
                exit;
            end;
            if not NewMoveXY(GetX(FindItem), GetY(FindItem), True, 1, True) then 
            begin
                AddToSystemJournal('Error: Не могу подойти к Recall скролом! x:='+IntToStr(GetX(FindItem))+' y:='+IntToStr(GetY(FindItem)));
                exit;
            end;   
            CheckConnect;
            CheckSave;
            CheckLag(50000);
            MoveItem(FindItem, SF-SC, SB, 0,0,0);
            wait(5000);        
        end; 
    end;
    result := True;
    Err := 0;    
end;

function Recall(into : Integer) : Boolean;
var x0, y0, t, i, CountG : Integer;
begin
    CheckConnect;
    result := True;
    x0 := GetX(Self);
    y0 := GetY(Self);
    If IsGump then
    begin
        CountG := GetGumpsCount;
        for i := 0 to CountG do CloseSimpleGump(i);  
    end;
    FindTypeEx($0EFA, $021E, Backpack, False);
    if FindCount > 0 then
    begin
        if CheckDead then exit;
        if CheckLock = 0 then
            if not Recharge then exit;  
        CheckSave;
        CheckLag(50000);
        UseObject(FindItem);
        if into = 0 then WaitGump(IntToStr(1024+RunHome));
        if into = 1 then WaitGump(IntToStr(1024+RunMinoc));
        t := 0;
        repeat 
            wait(100);
            t := t + 1;
            CheckConnect;
        until (GetX(Self) <> x0) or (GetY(Self) <> y0) or (t >= 500);
        if t >= 500 then 
        begin
            AddToSystemJournal('Error: Во время телепорта что то пошло не так...');
            Err := 1;
            result := False;
        end;
    end;
end;

function GoToMine : Boolean;
var
lock : Integer;
begin
    CheckConnect;
    Err := 1;
    result := False;
    lock := CheckLock;
    if lock < 0 then exit;
    if lock = 0 then
        if not Recall(1) then
        begin
            exit;
        end
        else
        begin
            lock := 1;
        end;
    if lock = 1 then
    begin
        CheckSave;
        if (GetX(Self) = 2558) and (GetY(Self) = 503) then NewMoveXY(2560,505,True,0,CheckWeight); 
        NewMoveXY(2558,503,True,0,CheckWeight);
        repeat
            wait(100);
            if CheckDead then Exit;
        until (GetX(Self) <> 2558) and (GetY(Self) <> 503);
    end;
    Err := 0;
    Result := True;   
end;

function GoToHome : Boolean;
var
CountG, i, lock : integer;
begin 
    CheckConnect;
    Err := 1;
    result := False;
    if CheckDead then exit;  
    lock := CheckLock; 
    CheckSave; 
    if lock = -1 then exit;
    if lock = 2 then
    begin     
        If IsGump then
        begin
            CountG := GetGumpsCount - 1;
            for i := 0 to CountG do CloseSimpleGump(i);  
        end;
        WaitGump('1025');
        NewMoveXY(2558, 497, True, 0, CheckWeight);
        repeat
            wait(100);
        until (GetY(Self) <> 497);
        lock := 1;   
    end;
    if lock = 1 then 
    begin
        if not Recall(0) then exit;; 
        for i := 0 to High(HD) do
        begin
            CheckSave;
            if not NewMoveXY(HD[i].x, HD[i].y, True, 0, CheckWeight) then
            begin
                AddToSystemJournal('Error: Не могу попасть к '+IntToStr(i+1)+' двери дома!');
                Exit;
            end;
            CheckConnect;
            if CheckDead then exit;
            CheckLag(50000);
            OpenDoor;
        end;
    end;
    if (GetX(Self) > HXMax) or (GetY(Self) > HYMax) or (GetX(Self) < HXMin) or (GetY(Self) < HYMin) then
    begin
        for i := 0 to High(HD) do
        begin
            if not NewMoveXY(HD[i].x, HD[i].y, True, 0, CheckWeight) then
            begin
                AddToSystemJournal('Error: Не могу попасть к '+IntToStr(i+1)+' двери дома!');
                Exit;
            end;
            CheckSave;
            CheckConnect;
            if CheckDead then exit;
            CheckLag(50000);
            OpenDoor;    
        end;
    end;
    if ((GetX(Self) <= HXMax) and (GetY(Self) <= HYMax)) or ((GetX(Self) >= HXMin) and (GetY(Self) >= HYMin)) then
    begin
        if not NewMoveXY(homeX, homeY, True, 0, CheckWeight) then
        begin    
            for i := 1 to High(HD) do
            begin
                if not NewMoveXY(HD[i].x, HD[i].y, True, 0, CheckWeight) then
                begin
                    AddToSystemJournal('Error: Не могу попасть к '+IntToStr(i+1)+' двери дома!');
                    Exit;
                end;   
                CheckSave;
                CheckConnect;
                if CheckDead then exit;
                CheckLag(50000);
                OpenDoor;    
            end;
        end;
    end;
    if (GetX(Self) <> homeX) and (GetY(Self) <> homeY) then
        if not NewMoveXY(homeX, homeY, True, 0, CheckWeight) then
            begin
                AddToSystemJournal('Error: Не могу попасть к ресурсам в доме!');
                Exit;
            end;    
    result := True;
    Err := 0;
end;

procedure OreStack;
var
CollorB : Word;
OreB : Cardinal;
begin
    CheckConnect;
    if CheckDead then exit;
    IgnoreReset;
    repeat 
        FindTypeEx($1BF2, $FFFF, Backpack, False);
        if FindCount > 0 then
        begin
            CollorB := GetColor(FindItem);
            OreB := FindItem;
            FindTypeEx($1BF2, CollorB, Ground, False);
            if FindCount > 0 then
            begin
                if not NewMoveXY(GetX(FindItem), GetY(FindItem), True, 0, CheckWeight) then exit;
                repeat 
                    FindTypeEx($1BF2, CollorB, Ground, False);
                    if FindCount <= 0 then
                    begin
                        CheckSave;
                        CheckConnect;
                        CheckLag(50000);
                        Drop(OreB, FindQuantity, 0,0,0);
                        wait(500);
                        break;
                    end;
                    if FindQuantity >= 60000 then Ignore(FindItem);
                    if FindQuantity < 60000 then
                    begin
                        CheckSave;
                        CheckConnect;
                        CheckLag(50000);
                        MoveItem(OreB, 60000 - FindQuantity, FindItem, 0,0,0);
                        wait(500);
                        break;
                    end;
                    FindTypeEx($1BF2, CollorB, Backpack, False);
                until FindCount <= 0;
            end
            else
            begin
                GoToHome;
                CheckSave;
                CheckConnect;
                CheckLag(50000);
                Drop(OreB, FindQuantity, 0,0,0);  
                wait(500);
            end;    
        end;   
        FindTypeEx($1BF2, $FFFF, Backpack, False);
    until FindCount <= 0;
end;

procedure DropResourse;
var
lock : Integer;
TrashC : Cardinal;
begin
    CheckConnect;
    if CheckDead then exit;
    repeat 
        GoToHome;             
        CheckSave;
        CheckLag(50000);
        FindTypeEx($19B9, $FFFF, Backpack, True);
        if FindCount > 0 then
        begin
            UseType($19B9, $FFFF);
            Wait(1000);
        end;
    until FindCount <= 0;
    lock := CheckLock;
    if lock = 0 then 
    begin
        OreStack;
        GoToHome;
        if Trash <= 0 then TrashC := Ground;
    end;
    repeat
        CheckSave;
        CheckLag(50000);
        CheckConnect;
        FindTypeEx($14ED,$ffff,backpack,False);
        if lock > 0 then Drop(FindItem, 1, 0,0,0);
        if lock = 0 then MoveItem(FindItem, FindQuantity, TrashC, 0,0,0);
        wait(500);
    until FindCount <= 0;
end;

function CreatePickAxe : Boolean;
var
t : integer;
tm : TDateTime;
begin
    Err := 1;
    result := False;
    CheckSave;
    CheckConnect;
    if CheckDead then exit;
    if MenuPresent then CloseMenu;
    if TargetPresent then CancelTarget;
    if MenuHookPresent then CancelMenu;
    FindTypeEx($1EBC, $FFFF, Backpack, False);
    if (FindCount <= 0) or (GetSkillValue('Tinkering') < 52) then
    begin
        if FindCount <= 0 then AddToSystemJournal('Error: В бекпаке нет Tinker Tools!');
        if GetSkillValue('Tinkering') < 52 then AddToSystemJournal('Error: Скил тинкеринга меньше 52!');
        Exit;    
    end;  
    CheckLag(50000);
    UseObject(FindItem);
    WaitForTarget(5000)
    t := 0;
    repeat
        wait(100);
        t := t + 1;
        CheckSave;
        CheckConnect;
    until TargetPresent or (t >= 150);
    AutoMenu('What','Deadly Tools');
    AutoMenu('What','Pickaxe');
    FindTypeEx($1BF2, $0000, Backpack, False);
    if (CountPickAxe * 4) > FindQuantity then
    begin
        AddToSystemJournal('Не хватает Iron Ore!');
        exit;
    end;
    WaitTargetObject(FindItem);
    t := 0;
    repeat
        wait(100);
        t := t + 1;
        CheckSave;
        CheckConnect;
    until not TargetPresent or (t >= 150);
    tm := Now;
    t := 0;
    repeat
        wait(100);
        t := t + 1;
    until (InJournalBetweenTimes('skilled enough|Success|You destroyed|You stop.|You are already holding the item', tm, Now) <> -1) or (t >= 150);
    if MenuHookPresent then CancelMenu();
    IgnoreReset;
    Err := 0;
    result := True;
end;

function CheckResours : Boolean;
var
t, t1, lock : Integer;
begin
    CheckConnect;
    result := False;
    Err := 1;
    CheckSave;
    if CheckDead then exit;
    //проверяем наличие и кол-во рунбук, должна быть одна
    FindTypeEx($0EFA, $021E, Backpack, False);
    if (FindCount <= 0) and (FindCount > 1) then
    begin
        AddToSystemJournal('Error: В бекпаке нет рунбуки или их больше одной!');
        Exit;
    end;    
    //проверяем кол-во еды в паке.
    FindTypeEx($097B, $FFFF, Backpack, False);
    if FindFullQuantity <= 5 then 
    begin
        CheckSave;
        CheckConnect;
        if CheckDead then exit;
        if not GoToHome then Exit;
        FindTypeEx($097B, $FFFF, Ground, False);
        if FindFullQuantity < CountFood then
        begin
            AddToSystemJournal('Error: Дома нет еды! Без еды я не буду работать!');
            Exit;
        end;
        CheckLag(50000);
        MoveItem(FindItem, CountFood, Backpack, 0,0,0);  
    end; 
    //Проверяем кирки
    CheckLag(50000);
    CheckSave;
    if ObjAtLayer(RhandLayer) <> 0 then
    begin
        disarm;
        wait(1000);
    end;
    FindTypeEx($0E85, $FFFF, Backpack, False);
    if FindCount <= 0 then
    begin
        CheckSave;
        CheckConnect;
        if CheckDead then exit;
        AddToSystemJournal('Проблема с кирками, их нет...');
        //если в настроках вбит серийник контейнера откуда брать кирки
        if PickAxeRules > 0 then
        begin
            AddToSystemJournal('Идём к контейнеру за кирками..');
            lock := CheckLock;
            if lock < 0 then exit;
            if (lock > 0) and (GoToHome = False) then exit;
            UseObject(PickAxeRules); 
            repeat                                            
                wait(100);
            until (LastContainer = PickAxeRules) or (t >= 500);
            if t >= 500 then
            begin
                AddToSystemJournal('Error: Не могу открыть контейнер с кирками!');
                exit;
            end;
            FindTypeEx($0E85, $FFFF, PickAxeRules, False);
            if FindCount <= 0 then
            begin
                AddToSystemJournal('Error: В контейнере нет кирок!');
                Exit;
            end;     
            if (FindCount > 0) and (FindCount <= CountPickAxe) then AddToSystemJournal('>>>>> Забрал последние кирки('+IntToStr(FindCount)+'), там больше нет!'); 
            CheckSave;
            t := CountPickAxe;
            t1 := 0;
            repeat
                CheckSave;
                CheckConnect;
                if CheckDead then exit;
                CheckLag(50000);
                FindTypeEx($0E85, $FFFF, PickAxeRules, False);
                if FindCount > 0 then
                begin
                    MoveItem(FindItem, 1, Backpack, 0,0,0);
                    wait(500);
                    Ignore(FindItem);
                    t := t - 1;
                    t1 := t1 + 1;
                    AddToSystemJournal('CountPickAxe := '+IntToStr(CountPickAxe)+'\'+IntToStr(t1)); 
                end;   
            until (FindCount <= 0) or (t <= 0);
            IgnoreReset;
        end;
        //если кирки крафтим стамостоятельно
        if PickAxeRules <= 0 then
        begin 
            AddToSystemJournal('Начинаю крафтить...');
            repeat
                CheckSave;
                CheckConnect;
                if CheckDead then exit;
                if not CreatePickAxe then exit;
                AddToSystemJournal('CountPickAxe := '+IntToStr(CountPickAxe)+'\'+IntToStr(FindCount));
                FindTypeEx($0E85, $FFFF, Backpack, False);
            until FindCount >= CountPickAxe;
        end;
        AddToSystemJournal('Продолжаем...'); 
    end;        
    result := True;
    Err := 0;
    IgnoreReset;    
end;

function CheckTrigger : Boolean;
var
t : Integer;
begin
    result := True;  
    t := 0;
    if CheckDead then exit;         
    CheckConnect;
    CheckSave;
    if (GetType(ObjAtLayer(RhandLayer)) = $0E85) then t := t + 1;
    FindTypeEx($0E85, $FFFF, Backpack, False);
    if FindCount > 0 then t := t + FindCount;
    if t <= 0 then
    begin
        if ((Trigger = -1) and (PickAxeRules = 0)) or (PickAxeRules = 0) then
        begin 
            if CheckLock = 2 then NewMoveXY(2593, 461, True, 0, CheckWeight);
            CheckConnect;
            CheckSave;
            CheckLag(50000);
            repeat
                CheckConnect;
                FindTypeEx($19B9, $0000, Backpack, False);
                if FindCount > 0 then
                begin
                    UseObject(FindItem);
                    wait(500);
                end;
            until FindCount <= 0;
            if not CheckResours then exit;
        end; 
        if PickAxeRules <> 0 then
        begin
            if not CheckResours then exit;
        end;
    end;
    CheckConnect;
    CheckSave; 
    if Trigger >= 0 then 
    begin
        if ((Trigger = 0) and (Weight >= 64000)) or ((Trigger > 0) and (Weight >= Trigger)) then
        begin
            if not GoToHome then exit;
            if not CheckResours then exit;
            DropResourse;
        end;
    end;  
    result := False;
end;

function CheckPickAxe : Boolean;
begin
    if CheckDead then exit;         
    CheckConnect;
    CheckSave;
    result := True;
    if (GetType(ObjAtLayer(RhandLayer)) = $0E85) then exit;
    if (GetType(ObjAtLayer(RhandLayer)) <> $0E85) then
    begin
        CheckLag(50000);
        Disarm;
        wait(500);
    end; 
    FindTypeEx($0E85, $FFFF, Backpack, False);
    if FindCount <= 0 then 
    begin   
        result := False;
        exit;
    end;
    if FindCount > 0 then
    begin
        Equip(RhandLayer,FindItem);
        wait(500);
    end;       
end;

procedure DropMap;
begin
    repeat
        if CheckDead then exit;         
        CheckConnect;
        CheckSave;
        FindTypeEx($14ED, $ffff, Backpack, False);
        if (FindCount > 0) then
        begin
            CheckSave;
            CheckLag(50000);
            Drop(FindItem, 0, 0, 0, 0);
            wait(300);
        end;
    until FindCount <= 0;
end;

procedure SetInfo;
begin


    //SetGlobal('stealth', 'TestVar1');
end;

procedure Mining(x, y, t : Word);
var
mn1, mf1, mf2, ms1, k : Integer;
ctime : TDateTime;
begin
    repeat
        if CheckDead then exit;         
        CheckConnect;
        CheckSave;
        if not CheckPickAxe then exit;
        if TargetPresent then CancelTarget;
        if WarMode = true then SetWarMode(false);
        ctime := Now;  
        CheckLag(50000);
        UseObject(ObjAtLayerEx(RhandLayer,self));
        WaitForTarget(5000);
        if TargetPresent then TargetToTile(t, x, y, GetZ(self));
        k := 0;
        repeat
            CheckConnect;
            wait(100);
            k := k + 1;
            mn1 := InJournalBetweenTimes('stop|mine or dig anything there.|Это слишком далеко.', ctime, Now);
            mf1 := InJournalBetweenTimes('you can', ctime, Now);
            mf2 := InJournalBetweenTimes('fail', ctime, Now);
            ms1 := InJournalBetweenTimes('way', ctime, Now);
        until (mn1<>-1) or (mf1<>-1) or (mf2<>-1) or (ms1<>-1) or (k > 300);
    until (mn1<>-1);
    DropMap;
end;

procedure RunSettings;
var
SL : TStringList;
i, t : Integer;
begin
    SL := TStringList.Create;
    StrBreakApart(homeDoor, ',', SL);
    if (SL.Count and 1) <> 0 then 
    begin
        AddToSystemJournal('Error: Проверьте настройки homeDoor!');
        exit;
    end;
    t := 0;
    for i := 0 to SL.Count - 1 do
    begin
        SetLength(HD, t + 1);
        HD[t].x := StrToInt(SL.Strings[i]);
        HD[t].y := StrToInt(SL.Strings[i+1]);
        i := i + 1;
        t := t + 1;       
    end;
    SL.Free;
    SL := TStringList.Create;
    StrBreakApart(homeCoord, ',', SL);
    if SL.Count <> 4 then 
    begin
        AddToSystemJournal('Error: Проверьте настройки homeCoord!');
        exit;
    end;
    HXMin := StrToInt(SL.Strings[0]);
    HXMax := StrToInt(SL.Strings[1]);
    HYMin := StrToInt(SL.Strings[2]);
    HYmax := StrToInt(SL.Strings[3]);       
    SL.Free;
end;

// определяем рабочие тайлы в "змейке"
procedure GetWorkTile;
var
i, i0, t, b : Integer;
begin
    t := 0;
    i := 0;
    AddToSystemJournal('Проверяем тайлы...');
    for i := 0 to High(Block) do
    begin
        if b < Round(100*i/High(Block)) then AddToSystemJournal('...'+IntToStr(Round(100*i/High(Block)))+'%');
        i0 := 0;
        repeat
            b := Round(100*i/High(Block));    
            if (Block[i].x = AllTile[i0].x) and (Block[i].y = AllTile[i0].y) then
            begin
                SetLength(Tile, t + 1);
                Tile[t].x := AllTile[i0].x;
                Tile[t].y := AllTile[i0].y;
                Tile[t].t := AllTile[i0].t;
                t := t + 1; 
                Break;        
            end; 
            i0 := i0 + 1;   
        until i0 >= High(AllTile);
    end;
    AddToSystemJournal('Проверка завершена.');
end;

// формируем змейку по 5 тайлов в ряд
procedure GetBlocks;
var
iY, Ymin, iX, t : Integer;

begin
    t := 0;
    Ymin := minY;
    repeat
        for iX := minX to maxX do
        begin
            for iY := Ymin to Ymin + 5 do
            begin 
                SetLength(Block, t + 1);
                Block[t].x := iX;
                Block[t].y := iY;
                t := t + 1;
            end; 
        end;
        for iX := maxX downto minX do
        begin
            for iY := Ymin to Ymin + 5 do
            begin
                SetLength(Block, t + 1);
                Block[t].x := iX;
                Block[t].y := iY;
                t := t + 1;
            end;
        end;
        Ymin := iY + 1;
    until ((maxX - minX)*(maxY - minY)) <= t;   
end;

// ищем все тайлы
function GetTiles : Boolean;
var
a : TFoundTilesArray;
TileType : array [0..24] of Integer;
i, i0, i1, b : Integer;
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
    i1 := 0;
    for i := 0 to 24 do
    begin
        b := GetStaticTilesArray(minX, minY, maxX, maxY, WorldNum, TileType[i], a);
        for i0 := 0 to b do
        begin 
            if a[i0].z <= 0 then 
            begin
                SetLength(AllTile, i1 + 1);
                AllTile[i1].t := a[i0].tile;
                AllTile[i1].x := a[i0].x;
                AllTile[i1].y := a[i0].y;
                i1 := i1 + 1;
            end;
        end;
    end;
    if High(AllTile) <= 0 then
    begin
        result := False;
        AddToSystemJournal('Error: Нашли 0 тайлов!');
        Exit;
    end;
    GetBlocks; 
    GetWorkTile;
    result := True;
end;





procedure test;
begin
    MoveOpenDoor := True;
    FindDistance := 18; 
    moveThroughNPC := 0;
    if not GetARStatus then SetARStatus(True);
    RunSettings;
    CheckConnect;
    if CheckDead then Err := 1;
    lock := CheckLock;
    if lock < 0 then 
        if not Recall(0) then Err := 1;
    if Err <= 0 then 
        if not GoToHome then Err := 1;
    if Err <= 0 then
        if not CheckResours then Err := 1;
    if Err <= 0 then
    begin 
        DropResourse;
        if not GetTiles then Err := 1;
    end;
    while Err <= 0 do
    begin
        for i := 0 to High(Tile) do
        begin 
            if CheckLock = 0 then
                if not GoToMine then break;
            if NewMoveXY(Tile[i].x, Tile[i].y, True, 2, CheckWeight) then
            begin
                AddToSystemJournal('Копаем '+IntToStr(i)+'/'+IntToStr(High(Tile))+' тайл'); 
                Mining(Tile[i].x, Tile[i].y, Tile[i].t);
            end;
            CheckTrigger;
            Hungry(1, Backpack);            
        end;        
    end; 
    if Err > 0 then AddToSystemJournal('END#ERROR: Скрипт завершился с ошибкой!!!');   
end;


procedure test1;
begin
    SetGlobal('Stealth', 'TestVar1', #13#10+'123');
end;

begin
    test1;
end.


{
Iron:'+IntToStr(CountEx($19B9,$0000,backpack))+'
'+'Copper:'+IntToStr(CountEx($19B9,$08EB,backpack))+'
'+'BD:'+IntToStr(CountEx($19B9,$0425,backpack))+'
'+'Pagan:'+IntToStr(CountEx($19B9,$050C,backpack))+'
'+'Silver:'+IntToStr(CountEx($19B9,$04EB,backpack))+'
'+'Spectral:'+IntToStr(CountEx($19B9,$0483,backpack))+'
'+'Lava:'+IntToStr(CountEx($19B9,$054E,backpack))+'
'+'Ice:'+IntToStr(CountEx($19B9,$04E7,backpack))+'
'+'Myt:'+IntToStr(CountEx($19B9,$07EC,backpack))+'
'+'Bas:'+IntToStr(CountEx($19B9,$0487,backpack))+'
'+'Sun:'+IntToStr(CountEx($19B9,$0AB1 ,backpack))+'
'+' Daed:'+IntToStr(CountEx($19B9,$0494,backpack))+
' Doom:'+IntToStr(CountEx($19B9,$07F5,backpack))+
' Zulu:'+IntToStr(CountEx($19B9,$0808,backpack))+
' Paradise:'+IntToStr(CountEx($0F2D,$06FE,backpack))+
' Hell:'+IntToStr(CountEx($0F2D,$0ADC,backpack))+
' Void:'+IntToStr(CountEx($0F21,$0B0E,backpack)
}