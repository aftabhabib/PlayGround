package com.desimeals.app.data.local;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Double;
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
public final class TransactionDao_Impl implements TransactionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TransactionEntity> __insertionAdapterOfTransactionEntity;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<TransactionEntity> __deletionAdapterOfTransactionEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  public TransactionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTransactionEntity = new EntityInsertionAdapter<TransactionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `transactions` (`id`,`customerId`,`type`,`amount`,`description`,`mealType`,`date`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TransactionEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getCustomerId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindLong(2, entity.getCustomerId());
        }
        final String _tmp = __converters.fromTransactionType(entity.getType());
        statement.bindString(3, _tmp);
        statement.bindDouble(4, entity.getAmount());
        statement.bindString(5, entity.getDescription());
        final String _tmp_1 = __converters.fromMealType(entity.getMealType());
        statement.bindString(6, _tmp_1);
        statement.bindLong(7, entity.getDate());
        statement.bindLong(8, entity.getCreatedAt());
      }
    };
    this.__deletionAdapterOfTransactionEntity = new EntityDeletionOrUpdateAdapter<TransactionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `transactions` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TransactionEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM transactions WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final TransactionEntity transaction,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTransactionEntity.insertAndReturnId(transaction);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final TransactionEntity transaction,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfTransactionEntity.handle(transaction);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TransactionEntity>> observeAll() {
    final String _sql = "SELECT * FROM transactions ORDER BY date DESC, id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transactions"}, new Callable<List<TransactionEntity>>() {
      @Override
      @NonNull
      public List<TransactionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customerId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfMealType = CursorUtil.getColumnIndexOrThrow(_cursor, "mealType");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<TransactionEntity> _result = new ArrayList<TransactionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TransactionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Long _tmpCustomerId;
            if (_cursor.isNull(_cursorIndexOfCustomerId)) {
              _tmpCustomerId = null;
            } else {
              _tmpCustomerId = _cursor.getLong(_cursorIndexOfCustomerId);
            }
            final TransactionType _tmpType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfType);
            _tmpType = __converters.toTransactionType(_tmp);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final MealType _tmpMealType;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfMealType);
            _tmpMealType = __converters.toMealType(_tmp_1);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new TransactionEntity(_tmpId,_tmpCustomerId,_tmpType,_tmpAmount,_tmpDescription,_tmpMealType,_tmpDate,_tmpCreatedAt);
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
  public Flow<List<TransactionEntity>> observeForCustomer(final long customerId) {
    final String _sql = "SELECT * FROM transactions WHERE customerId = ? ORDER BY date DESC, id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, customerId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transactions"}, new Callable<List<TransactionEntity>>() {
      @Override
      @NonNull
      public List<TransactionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customerId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfMealType = CursorUtil.getColumnIndexOrThrow(_cursor, "mealType");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<TransactionEntity> _result = new ArrayList<TransactionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TransactionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Long _tmpCustomerId;
            if (_cursor.isNull(_cursorIndexOfCustomerId)) {
              _tmpCustomerId = null;
            } else {
              _tmpCustomerId = _cursor.getLong(_cursorIndexOfCustomerId);
            }
            final TransactionType _tmpType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfType);
            _tmpType = __converters.toTransactionType(_tmp);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final MealType _tmpMealType;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfMealType);
            _tmpMealType = __converters.toMealType(_tmp_1);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new TransactionEntity(_tmpId,_tmpCustomerId,_tmpType,_tmpAmount,_tmpDescription,_tmpMealType,_tmpDate,_tmpCreatedAt);
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
  public Flow<Double> observeTotalByType(final TransactionType type) {
    final String _sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    final String _tmp = __converters.fromTransactionType(type);
    _statement.bindString(_argIndex, _tmp);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transactions"}, new Callable<Double>() {
      @Override
      @NonNull
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final double _tmp_1;
            _tmp_1 = _cursor.getDouble(0);
            _result = _tmp_1;
          } else {
            _result = 0.0;
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
  public Flow<Double> observeTotalByTypeInRange(final TransactionType type, final long from,
      final long to) {
    final String _sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = ? AND date BETWEEN ? AND ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    final String _tmp = __converters.fromTransactionType(type);
    _statement.bindString(_argIndex, _tmp);
    _argIndex = 2;
    _statement.bindLong(_argIndex, from);
    _argIndex = 3;
    _statement.bindLong(_argIndex, to);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transactions"}, new Callable<Double>() {
      @Override
      @NonNull
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final double _tmp_1;
            _tmp_1 = _cursor.getDouble(0);
            _result = _tmp_1;
          } else {
            _result = 0.0;
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
  public Flow<Double> observeChargesForCustomer(final long customerId) {
    final String _sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE customerId = ? AND type = 'CHARGE'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, customerId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transactions"}, new Callable<Double>() {
      @Override
      @NonNull
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final double _tmp;
            _tmp = _cursor.getDouble(0);
            _result = _tmp;
          } else {
            _result = 0.0;
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
  public Flow<Double> observePaymentsForCustomer(final long customerId) {
    final String _sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE customerId = ? AND type = 'PAYMENT'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, customerId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transactions"}, new Callable<Double>() {
      @Override
      @NonNull
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final double _tmp;
            _tmp = _cursor.getDouble(0);
            _result = _tmp;
          } else {
            _result = 0.0;
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
