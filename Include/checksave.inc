procedure CheckSave; 
var Time : TDateTime; 
begin 
Time:= Now - (0.5 / 1440); 
if InJournalBetweenTimes('Сохранение мира.',Time,Now) >= 0 then Wait(30000) 
end;