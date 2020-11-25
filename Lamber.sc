Program AutoLumber_SW;

//{$Include 'all.inc'}

const
  /////////////////////////////////////////////////
  // Обязательные к изменению настройки скрипта  //
  Sunduk = $40044DE1;                            //
  //                                             // 
  // Координаты точки перед сундуком             //
  xTileSunduk = 2232;                            //
  yTileSunduk = 1197;                            //
  //                                             //
  /////////////////////////////////////////////////
 
  // Возможные к изменению настройки скрипта                                 
  MyMaxWeight = 640;  // Максимальный вес
  Hatchet1 = $0F45;   // Тип топора1
  Hatchet2 = $0F45;   // Тип топора2 (перевёрнутый)
 
  // Размерности массивов
  iTTileCount = 39;   // Типы тайлов деревьев (менять, только при редактировании массива)
  iCTileCount = 1;    // Кол-во точек (центров поляны), в которых хотим собирать информацию о деревьях (поляна = 30х30 тайлов)

  // Журнал
  Msg1 = 'You chop some ordinary logs and put them into your backpack.';
  Msg2 = 'not enough wood here to harvest.';
  Msg3 = 'hack';
  Msg4 = 'You decide not to chop wood';
  Msg5 = 'There is nothing';
  Msg6 = 'appears immune';
  Msg7 = 'Try chopping';
  Msg8 = 'reach this';
   
  // Прочее
  RunSpeed = 250;
  iRadiusSearch = 30; // Радиус (не диаметр!) поиска деревьев в тайлах, относительно персонажа
  Logs = $1BDD;       // Тип логов
  WoodType = $0F90;   // Тип дедвудов
   
type
  ChopTile = Record
    x, y : Integer;
  end;
   
var
  Regs : array [1..3] of Cardinal;
  FoundTilesArray : TFoundTilesArray;
  TempFoundTilesArray, ChopTilesArray : array of TFoundTile;
  TreeTile:array [0..iTTileCount] of word;
  ChopTiles : array[1..iCTileCount] of ChopTile;
  ctime : TDateTime;
  i : Integer;
 
// Инициализация массива типов тайлов деревьев
procedure InitTTilesArray;
  begin
   TreeTile[0]:=3274;
   TreeTile[1]:=3275;
   TreeTile[2]:=3277;
   TreeTile[3]:=3280;

   TreeTile[4]:=3283;
   TreeTile[5]:=3286;
   TreeTile[6]:=3288;
   TreeTile[7]:=3290;

   TreeTile[8]:=3293;
   TreeTile[9]:=3296;
   TreeTile[10]:=3299;
   TreeTile[11]:=3302;

   TreeTile[12]:=3320;
   TreeTile[13]:=3323;
   TreeTile[14]:=3326;
   TreeTile[15]:=3329;

   TreeTile[16]:=3393;
   TreeTile[17]:=3394;
   TreeTile[18]:=3395;
   TreeTile[19]:=3396;

   TreeTile[20]:=3415;
   TreeTile[21]:=3416;
   TreeTile[22]:=3417;
   TreeTile[23]:=3418;

   TreeTile[24]:=3419;
   TreeTile[25]:=3438;
   TreeTile[26]:=3439;
   TreeTile[27]:=3440;

   TreeTile[28]:=3441;
   TreeTile[29]:=3442;
   TreeTile[30]:=3460;
   TreeTile[31]:=3461;

   TreeTile[32]:=3462;
   TreeTile[33]:=3476;
   TreeTile[34]:=3478;
   TreeTile[35]:=3480;

   TreeTile[36]:=3482;
   TreeTile[37]:=3484;
   TreeTile[38]:=3492;
   TreeTile[39]:=3496;
  end;

// Инициализация массива координат для поиска деревьев
procedure InitCTilesArray;
  begin
    ChopTiles[1].x := 2351;  // Координаты первой точки, на которую идем и ищем деревья
    ChopTiles[1].y := 1205;
   
    {ChopTiles[2].x := 2357;  // Координаты второй точки, на которую идем и ищем деревья
    ChopTiles[2].y := 3044;
   
    ChopTiles[3].x := 2331;  // Координаты третьей точки, на которую идем и ищем деревья
    ChopTiles[3].y := 3096;
   
    ChopTiles[4].x := 2369;  // Координаты третьей точки, на которую идем и ищем деревья
    ChopTiles[4].y := 3120;     }
  end;

// Инициализация системных переменных
procedure InitSystem;
  begin
    SetRunUnmountTimer(RunSpeed);
    SetArrayLength(ChopTilesArray, 1);
  end;

// Инициализация регов
procedure InitReg;
  begin
    Regs[1] := $0F85;      // Ginseng
    Regs[2] := $0F88;      // Nightshade
    Regs[3] := $0F86;      // Mandrake Roots
  end;
 
// Поиск деревьев
procedure SearchTree;
  var
  i, j : Integer;
  iFoundTilesArrayCount : word;
  iTempFoundTilesArrayCount : Integer;
 
  begin
    for i:= 0 to iTTileCount do
      begin
        iFoundTilesArrayCount := GetStaticTilesArray((GetX(Self) - iRadiusSearch), (GetY(Self) - iRadiusSearch), (GetX(Self) + iRadiusSearch), (GetY(Self) + iRadiusSearch), 1, TreeTile[i], FoundTilesArray);
        if iFoundTilesArrayCount > 0 then
          begin
            SetArrayLength(TempFoundTilesArray, Length(TempFoundTilesArray) + iFoundTilesArrayCount);
            for j := 0 to iFoundTilesArrayCount - 1 do
              begin           
                TempFoundTilesArray[iTempFoundTilesArrayCount + j] := FoundTilesArray[j];
              end;
            iTempFoundTilesArrayCount := iTempFoundTilesArrayCount + iFoundTilesArrayCount; 
          end;
      end;
    AddToSystemJournal('Найдено деревьев: ' + IntToStr(iTempFoundTilesArrayCount));     
  end;

// Чистим записи дубликаты (Vizit0r :P)
procedure ClearDuplicate;
  var
  i, j : Integer;
 
  begin
    ChopTilesArray[Length(ChopTilesArray) - 1] := TempFoundTilesArray[0];
    for i:=1 to Length(TempFoundTilesArray) - 1 do
      begin
        for j:=0 to Length(ChopTilesArray) - 1 do
          if (ChopTilesArray[j] = TempFoundTilesArray[i]) then
            break;
        if j > Length(ChopTilesArray) - 1 then
          begin
            SetArrayLength(ChopTilesArray, Length(ChopTilesArray) + 1);
            ChopTilesArray[Length(ChopTilesArray) - 1] := TempFoundTilesArray[i]; 
          end;
      end;
      AddToSystemJournal('После отсеивания дубликатов, осталось деревьев:' + IntToStr(Length(ChopTilesArray))); 
  end;

// Возводим в степень 2 (Shinma)
function sqr(a:LongInt):LongInt;
  begin
    result:=a*a;
  end;

// Вычисляем длину вектора (Shinma)
function vector_length(c_2:TFoundTile):LongInt;
  begin
    result:=Round(sqrt(sqr(GetX(self)-c_2.X)+sqr(GetY(self)-c_2.Y)));
  end;

// «Быстрая сортировка» по длине вектора, от центра последней поляны ко всем собранным координатам деревьев   
procedure QuickSort(A: array of TFoundTile; l,r: integer);
  var
  i, j: Integer;
  x, y: TFoundTile;
 
  begin
    i := l;
    j := r;
    x := A[((l + r) div 2)];
    repeat
      while vector_length(A[i]) < vector_length(x) do inc(i);
      while vector_length(x) < vector_length(A[j]) do dec(j);
      if not (i>j) then
        begin
          y:= A[i];
          A[i]:= A[j];
          A[j]:= y;
          inc(i);
          dec(j);
        end;
    until i>j;
    if l < j then QuickSort(ChopTilesArray, l,j);
    if i < r then QuickSort(ChopTilesArray, i,r);
  end;

// Находим, исключаем дубликаты, сортируем деревья
procedure MarkTrees;
  begin
    for i:= 1 to iCTileCount do
      begin
        NewMoveXY(ChopTiles[i].x, ChopTiles[i].y, False, 1, False);
        SearchTree;
        AddToSystemJournal('Всего найдено деревьев: ' + IntToStr(Length(TempFoundTilesArray)));
        ClearDuplicate;
      end;
    QuickSort(ChopTilesArray, 0, Length(ChopTilesArray) - 1);
  end;
 
// Разгрузка (Edred)
procedure Discharge;
  // разгружаем нарубленное в сундук
  // нарубленное - реги в массиве Regs[1..3]
  // логи - константа Logs
  var
  m, tmpcnt : integer;
  tmpid, tmpstack, tmpcolor : Cardinal;
  tmpname : String;
  begin
    AddToSystemJournal('Разгружаемся');
    //waitconnection(3000);
    if Dead then exit;
    UseObject(Sunduk);
    wait(1000);
    //checksave;
    // выложим реги
    for m := 1 to 3 do
      begin
        tmpcnt := 0;
        Repeat
          tmpid := Findtype(Regs[m],backpack);
          if tmpid = 0 then break;
          addtosystemjournal( 'Найдено ' + inttostr(GetQuantity(tmpid)) + ' regs');
          tmpcnt := tmpcnt + 1;
          if tmpcnt > 10 then
            begin
              addtosystemjournal('Ошибка: не могу переместить regs!');
              wait(15000);
            end;
          MoveItem(tmpid,GetQuantity(tmpid),Sunduk,0,0,0);
          wait(1000);
          //CheckSave;
        until tmpid = 0;
      end;
    // выложим дид вуды
    tmpcnt := 0;
    Repeat
      tmpid := Findtype(WoodType,backpack);
      if tmpid = 0 then break;
      addtosystemjournal( 'Найдено ' + inttostr(GetQuantity(tmpid)) + ' dead woods');
      tmpcnt := tmpcnt + 1;
      if tmpcnt > 10 then
        begin
          addtosystemjournal('Ошибка: не могу переместить dead woods!');
          wait(15000);
        end;
      tmpstack := Findtype(WoodType,Sunduk);
      // Если не найден в банке - тогда просто в контейнер
      if tmpstack = 0 then tmpstack := Sunduk;
      MoveItem(tmpid,GetQuantity(tmpid),tmpstack,0,0,0);
      wait(1000);
      //CheckSave;
    until tmpid = 0;
    // выложим логи
    tmpcnt := 0;
    Repeat
      tmpid := Findtype(Logs,backpack);
      if tmpid = 0 then break;
      tmpcolor := GetColor(tmpid);
      tmpname := ' unknown logs';
      case tmpcolor of
        $0000 : tmpname := ' logs';
        $037F : tmpname := ' Grave logs';
        $0039 : tmpname := ' Willow logs';
        $0026 : tmpname := ' Maple logs';
        $0405 : tmpname := ' Oak logs';
        $0994 : tmpname := ' Bloody logs';
        $048A : tmpname := ' Nature logs';
        $0898 : tmpname := ' Spirits logs';
      end;
      addtosystemjournal( 'Найдено ' + inttostr(GetQuantity(tmpid)) + tmpname);
      tmpcnt := tmpcnt + 1;
      if tmpcnt > 10 then
        begin
          addtosystemjournal('Ошибка: не могу переместить логи');
          wait(15000);
        end;
      repeat
        tmpstack := FindtypeEx(Logs,tmpcolor,Sunduk,False);
        if GetQuantity(tmpstack) >= 65000 then Ignore(tmpstack);
      until (tmpstack = 0) OR (GetQuantity(tmpstack) < 65000);
      // Если не найден в сундуке - тогда просто в контейнер
      if tmpstack = 0 then tmpstack := Sunduk;
      MoveItem(tmpid,GetQuantity(tmpid),tmpstack,0,0,0);
      wait(1000);
      //CheckSave;
    until tmpid = 0;
    IgnoreReset;
    AddToSystemJournal('Разгрузка закончена');
  end;

// Идем к сундуку и выгружаемся
procedure UnloadOrDead;
  begin
    NewMoveXY(xTileSunduk, yTileSunduk, false, 1, false);
    if not Dead then begin Discharge; end
    else begin AddToSystemJournal('Персонаж мертв.'); SetARStatus(False); Disconnect; end;     
  end;
 
// Рубим дерево (Edred)
function LumbCurTree(tile,x,y,z : Integer) : Boolean;
  // рубим указанный тайл. Возвращаем false если перевес или чар мертв.
  var
  q, m1, m2, m3, m4, m5, m6, m7, m8, CountFizzle, NextTree : integer;
 
  begin
    Result := true;
    CountFizzle := 0;
    repeat
      if WarMode = true then SetWarMode(false);
      if TargetPresent then CancelTarget;
      ctime := Now;
      if Dead then begin Result := false; exit; end;
      if UseType(Hatchet1,$FFFF) = 0 then
        begin
          if UseType(Hatchet2,$FFFF) = 0 then
            begin
              Result := false;
              exit;
            end;
        end;
      WaitForTarget(5000);
      If TargetPresent then TargetToTile(tile, x, y, z);
      q := 0;
      repeat
        wait(100);
        q := q + 1;
        //checksave;
        m1 := InJournalBetweenTimes(Msg1, ctime, Now);
        m2 := InJournalBetweenTimes(Msg2, ctime, Now);
        m3 := InJournalBetweenTimes(Msg3, ctime, Now);
        m4 := InJournalBetweenTimes(Msg4, ctime, Now);
        m5 := InJournalBetweenTimes(Msg5, ctime, Now);
        m6 := InJournalBetweenTimes(Msg6, ctime, Now);
        m7 := InJournalBetweenTimes(Msg7, ctime, Now);
        m8 := InJournalBetweenTimes(Msg8, ctime, Now); 
      until (m1<>-1) or (m2<>-1) or (m3<>-1) or (m4<>-1) or (m5<>-1) or (m6<>-1) or (m7<>-1) or (m8<>-1) or Dead or (q > 150);
      if (m2<>-1) or (m3<>-1) or (m4<>-1) then CountFizzle := CountFizzle + 1;
      if Dead or (Weight > MyMaxWeight) then begin Result := false; exit; end;
      if (q > 150) then NextTree := NextTree + 1;
    until (m5<>-1) OR (m6<>-1) OR (m7<>-1) OR (m8<>-1) OR (CountFizzle = 10) OR (NextTree > 3);
    if NextTree >= 3 then NextTree := 0;
  end;

       
// Главная функция       
Begin   
  InitTTilesArray;
  InitCTilesArray;
  InitSystem;
  InitReg;
  MarkTrees; 
 
  repeat 
    for i:= 0 to Length(ChopTilesArray) - 1 do
      begin
        NewMoveXY(ChopTilesArray[i].x, ChopTilesArray[i].y, false, 1, false);
        if not LumbCurTree(ChopTilesArray[i].tile, ChopTilesArray[i].x, ChopTilesArray[i].y, ChopTilesArray[i].z) then UnloadOrDead;
      end;
  until Dead; 
End.