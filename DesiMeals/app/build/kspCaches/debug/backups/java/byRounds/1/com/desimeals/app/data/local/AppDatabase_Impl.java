package com.desimeals.app.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile CustomerDao _customerDao;

  private volatile TransactionDao _transactionDao;

  private volatile MealDao _mealDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `customers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `phone` TEXT NOT NULL, `address` TEXT NOT NULL, `monthlyRate` REAL NOT NULL, `isActive` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `customerId` INTEGER, `type` TEXT NOT NULL, `amount` REAL NOT NULL, `description` TEXT NOT NULL, `mealType` TEXT NOT NULL, `date` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, FOREIGN KEY(`customerId`) REFERENCES `customers`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_customerId` ON `transactions` (`customerId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_date` ON `transactions` (`date`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `meal_entries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `customerId` INTEGER NOT NULL, `date` INTEGER NOT NULL, `breakfast` INTEGER NOT NULL, `lunch` INTEGER NOT NULL, `dinner` INTEGER NOT NULL, `notes` TEXT NOT NULL, FOREIGN KEY(`customerId`) REFERENCES `customers`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_entries_customerId` ON `meal_entries` (`customerId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_entries_date` ON `meal_entries` (`date`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '266db9f1f2fa1d4df51319c00a3c5788')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `customers`");
        db.execSQL("DROP TABLE IF EXISTS `transactions`");
        db.execSQL("DROP TABLE IF EXISTS `meal_entries`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsCustomers = new HashMap<String, TableInfo.Column>(7);
        _columnsCustomers.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("phone", new TableInfo.Column("phone", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("address", new TableInfo.Column("address", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("monthlyRate", new TableInfo.Column("monthlyRate", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCustomers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCustomers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCustomers = new TableInfo("customers", _columnsCustomers, _foreignKeysCustomers, _indicesCustomers);
        final TableInfo _existingCustomers = TableInfo.read(db, "customers");
        if (!_infoCustomers.equals(_existingCustomers)) {
          return new RoomOpenHelper.ValidationResult(false, "customers(com.desimeals.app.data.local.CustomerEntity).\n"
                  + " Expected:\n" + _infoCustomers + "\n"
                  + " Found:\n" + _existingCustomers);
        }
        final HashMap<String, TableInfo.Column> _columnsTransactions = new HashMap<String, TableInfo.Column>(8);
        _columnsTransactions.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("customerId", new TableInfo.Column("customerId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("mealType", new TableInfo.Column("mealType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("date", new TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTransactions = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysTransactions.add(new TableInfo.ForeignKey("customers", "CASCADE", "NO ACTION", Arrays.asList("customerId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesTransactions = new HashSet<TableInfo.Index>(2);
        _indicesTransactions.add(new TableInfo.Index("index_transactions_customerId", false, Arrays.asList("customerId"), Arrays.asList("ASC")));
        _indicesTransactions.add(new TableInfo.Index("index_transactions_date", false, Arrays.asList("date"), Arrays.asList("ASC")));
        final TableInfo _infoTransactions = new TableInfo("transactions", _columnsTransactions, _foreignKeysTransactions, _indicesTransactions);
        final TableInfo _existingTransactions = TableInfo.read(db, "transactions");
        if (!_infoTransactions.equals(_existingTransactions)) {
          return new RoomOpenHelper.ValidationResult(false, "transactions(com.desimeals.app.data.local.TransactionEntity).\n"
                  + " Expected:\n" + _infoTransactions + "\n"
                  + " Found:\n" + _existingTransactions);
        }
        final HashMap<String, TableInfo.Column> _columnsMealEntries = new HashMap<String, TableInfo.Column>(7);
        _columnsMealEntries.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMealEntries.put("customerId", new TableInfo.Column("customerId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMealEntries.put("date", new TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMealEntries.put("breakfast", new TableInfo.Column("breakfast", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMealEntries.put("lunch", new TableInfo.Column("lunch", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMealEntries.put("dinner", new TableInfo.Column("dinner", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMealEntries.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMealEntries = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysMealEntries.add(new TableInfo.ForeignKey("customers", "CASCADE", "NO ACTION", Arrays.asList("customerId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesMealEntries = new HashSet<TableInfo.Index>(2);
        _indicesMealEntries.add(new TableInfo.Index("index_meal_entries_customerId", false, Arrays.asList("customerId"), Arrays.asList("ASC")));
        _indicesMealEntries.add(new TableInfo.Index("index_meal_entries_date", false, Arrays.asList("date"), Arrays.asList("ASC")));
        final TableInfo _infoMealEntries = new TableInfo("meal_entries", _columnsMealEntries, _foreignKeysMealEntries, _indicesMealEntries);
        final TableInfo _existingMealEntries = TableInfo.read(db, "meal_entries");
        if (!_infoMealEntries.equals(_existingMealEntries)) {
          return new RoomOpenHelper.ValidationResult(false, "meal_entries(com.desimeals.app.data.local.MealEntryEntity).\n"
                  + " Expected:\n" + _infoMealEntries + "\n"
                  + " Found:\n" + _existingMealEntries);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "266db9f1f2fa1d4df51319c00a3c5788", "b6e94dde0ef7b59abbff0a62d2f97055");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "customers","transactions","meal_entries");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `customers`");
      _db.execSQL("DELETE FROM `transactions`");
      _db.execSQL("DELETE FROM `meal_entries`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(CustomerDao.class, CustomerDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TransactionDao.class, TransactionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MealDao.class, MealDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public CustomerDao customerDao() {
    if (_customerDao != null) {
      return _customerDao;
    } else {
      synchronized(this) {
        if(_customerDao == null) {
          _customerDao = new CustomerDao_Impl(this);
        }
        return _customerDao;
      }
    }
  }

  @Override
  public TransactionDao transactionDao() {
    if (_transactionDao != null) {
      return _transactionDao;
    } else {
      synchronized(this) {
        if(_transactionDao == null) {
          _transactionDao = new TransactionDao_Impl(this);
        }
        return _transactionDao;
      }
    }
  }

  @Override
  public MealDao mealDao() {
    if (_mealDao != null) {
      return _mealDao;
    } else {
      synchronized(this) {
        if(_mealDao == null) {
          _mealDao = new MealDao_Impl(this);
        }
        return _mealDao;
      }
    }
  }
}
