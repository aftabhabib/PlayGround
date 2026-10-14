package com.desimeals.app.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MealDao_Impl implements MealDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MealEntryEntity> __insertionAdapterOfMealEntryEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteForCustomerDay;

  public MealDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMealEntryEntity = new EntityInsertionAdapter<MealEntryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `meal_entries` (`id`,`customerId`,`date`,`breakfast`,`lunch`,`dinner`,`notes`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MealEntryEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getCustomerId());
        statement.bindLong(3, entity.getDate());
        final int _tmp = entity.getBreakfast() ? 1 : 0;
        statement.bindLong(4, _tmp);
        final int _tmp_1 = entity.getLunch() ? 1 : 0;
        statement.bindLong(5, _tmp_1);
        final int _tmp_2 = entity.getDinner() ? 1 : 0;
        statement.bindLong(6, _tmp_2);
        statement.bindString(7, entity.getNotes());
      }
    };
    this.__preparedStmtOfDeleteForCustomerDay = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM meal_entries WHERE customerId = ? AND date = ?";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final MealEntryEntity entry, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfMealEntryEntity.insertAndReturnId(entry);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteForCustomerDay(final long customerId, final long dayStart,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteForCustomerDay.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, customerId);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, dayStart);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteForCustomerDay.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MealEntryEntity>> observeForDay(final long dayStart) {
    final String _sql = "SELECT * FROM meal_entries WHERE date = ? ORDER BY customerId ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, dayStart);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"meal_entries"}, new Callable<List<MealEntryEntity>>() {
      @Override
      @NonNull
      public List<MealEntryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customerId");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfBreakfast = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast");
          final int _cursorIndexOfLunch = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch");
          final int _cursorIndexOfDinner = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<MealEntryEntity> _result = new ArrayList<MealEntryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MealEntryEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpCustomerId;
            _tmpCustomerId = _cursor.getLong(_cursorIndexOfCustomerId);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final boolean _tmpBreakfast;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfBreakfast);
            _tmpBreakfast = _tmp != 0;
            final boolean _tmpLunch;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfLunch);
            _tmpLunch = _tmp_1 != 0;
            final boolean _tmpDinner;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfDinner);
            _tmpDinner = _tmp_2 != 0;
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            _item = new MealEntryEntity(_tmpId,_tmpCustomerId,_tmpDate,_tmpBreakfast,_tmpLunch,_tmpDinner,_tmpNotes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<MealEntryEntity>> observeForCustomer(final long customerId) {
    final String _sql = "SELECT * FROM meal_entries WHERE customerId = ? ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, customerId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"meal_entries"}, new Callable<List<MealEntryEntity>>() {
      @Override
      @NonNull
      public List<MealEntryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customerId");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfBreakfast = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast");
          final int _cursorIndexOfLunch = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch");
          final int _cursorIndexOfDinner = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<MealEntryEntity> _result = new ArrayList<MealEntryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MealEntryEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpCustomerId;
            _tmpCustomerId = _cursor.getLong(_cursorIndexOfCustomerId);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final boolean _tmpBreakfast;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfBreakfast);
            _tmpBreakfast = _tmp != 0;
            final boolean _tmpLunch;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfLunch);
            _tmpLunch = _tmp_1 != 0;
            final boolean _tmpDinner;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfDinner);
            _tmpDinner = _tmp_2 != 0;
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            _item = new MealEntryEntity(_tmpId,_tmpCustomerId,_tmpDate,_tmpBreakfast,_tmpLunch,_tmpDinner,_tmpNotes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getForCustomerDay(final long customerId, final long dayStart,
      final Continuation<? super MealEntryEntity> $completion) {
    final String _sql = "SELECT * FROM meal_entries WHERE customerId = ? AND date = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, customerId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, dayStart);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MealEntryEntity>() {
      @Override
      @Nullable
      public MealEntryEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customerId");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfBreakfast = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast");
          final int _cursorIndexOfLunch = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch");
          final int _cursorIndexOfDinner = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final MealEntryEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpCustomerId;
            _tmpCustomerId = _cursor.getLong(_cursorIndexOfCustomerId);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final boolean _tmpBreakfast;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfBreakfast);
            _tmpBreakfast = _tmp != 0;
            final boolean _tmpLunch;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfLunch);
            _tmpLunch = _tmp_1 != 0;
            final boolean _tmpDinner;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfDinner);
            _tmpDinner = _tmp_2 != 0;
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            _result = new MealEntryEntity(_tmpId,_tmpCustomerId,_tmpDate,_tmpBreakfast,_tmpLunch,_tmpDinner,_tmpNotes);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
