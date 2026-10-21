import java.util.*;

public class CompetencyFrameworkSystem {

    // ==========================================
    // 1. DỮ LIỆU KHUNG NĂNG LỰC DÙNG CHUNG
    // ==========================================
    public static class Competency {
        private String code;
        private String name;
        private String category; // CORE, FUNCTIONAL, MANAGEMENT
        private Map<Integer, String> levels;

        public Competency(String code, String name, String category) {
            this.code = code;
            this.name = name;
            this.category = category;
            this.levels = new HashMap<>();
        }

        public void addLevel(int level, String description) {
            this.levels.put(level, description);
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public String getLevelDescription(int level) { return levels.getOrDefault(level, "Chưa định nghĩa"); }
    }

    // ==========================================
    // 2. DANH MỤC CHỨC DANH & CẤP BẬC
    // ==========================================
    public static class JobTitle {
        private String code;
        private String title;
        private String rankLevel; // Junior, Mid, Senior, Lead, Manager

        public JobTitle(String code, String title, String rankLevel) {
            this.code = code;
            this.title = title;
            this.rankLevel = rankLevel;
        }

        public String getCode() { return code; }
        public String getTitle() { return title; }
        public String getRankLevel() { return rankLevel; }
    }

    // ==========================================
    // 3. THIẾT LẬP TIÊU CHÍ, TRỌNG SỐ & CẤP ĐỘ CẦN ĐẠT
    // ==========================================
    public static class CompetencyRequirement {
        private String competencyCode;
        private int requiredLevel;
        private double weight; // Ví dụ: 0.20 tương ứng 20%

        public CompetencyRequirement(String competencyCode, int requiredLevel, double weight) {
            this.competencyCode = competencyCode;
            this.requiredLevel = requiredLevel;
            this.weight = weight;
        }

        public String getCompetencyCode() { return competencyCode; }
        public int getRequiredLevel() { return requiredLevel; }
        public double getWeight() { return weight; }
    }

    // ==========================================
    // 4. BỘ QUẢN LÝ DỮ LIỆU & SINH PHIẾU PHỎNG VẤN
    // ==========================================
    private Map<String, Competency> competencyRepository = new HashMap<>();
    private Map<String, JobTitle> jobTitleRepository = new HashMap<>();
    private Map<String, List<CompetencyRequirement>> jobMappings = new HashMap<>();

    public void registerCompetency(Competency comp) {
        competencyRepository.put(comp.getCode(), comp);
    }

    public void registerJobTitle(JobTitle job) {
        jobTitleRepository.put(job.getCode(), job);
    }

    public void mapCompetencyToJob(String jobCode, List<CompetencyRequirement> requirements) {
        jobMappings.put(jobCode, requirements);
    }

    public String generateInterviewSheet(String jobCode, String candidateName, String interviewerName, String dateStr) {
        JobTitle job = jobTitleRepository.get(jobCode);
        if (job == null) {
            return "Lỗi: Không tìm thấy mã chức danh " + jobCode;
        }

        List<CompetencyRequirement> requirements = jobMappings.getOrDefault(jobCode, Collections.emptyList());

        StringBuilder sb = new StringBuilder();
        sb.append("================================================================================\n");
        sb.append(String.format("%55s\n", "PHIẾU ĐÁNH GIÁ PHỎNG VẤN TUYỂN DỤNG"));
        sb.append("================================================================================\n");
        sb.append(String.format("Ứng viên         : %s\n", candidateName));
        sb.append(String.format("Vị trí ứng tuyển : %s (Mã: %s)\n", job.getTitle(), job.getCode()));
        sb.append(String.format("Cấp bậc          : %s\n", job.getRankLevel()));
        sb.append(String.format("Người phỏng vấn  : %s\n", interviewerName));
        sb.append(String.format("Ngày phỏng vấn   : %s\n", dateStr));
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format("%-4s | %-5s | %-30s | %-8s | %-8s | %-10s\n", 
                "STT", "Mã", "Tên Năng Lực", "Trọng số", "Yêu cầu", "Điểm (1-5)"));
        sb.append("--------------------------------------------------------------------------------\n");

        double totalWeight = 0;
        int stt = 1;
        List<String> questionsSection = new ArrayList<>();

        for (CompetencyRequirement req : requirements) {
            Competency comp = competencyRepository.get(req.getCompetencyCode());
            if (comp == null) continue;

            double weightPercent = req.getWeight() * 100;
            totalWeight += req.getWeight();
            String levelDesc = comp.getLevelDescription(req.getRequiredLevel());

            // Thêm dòng tiêu chí vào bảng
            sb.append(String.format("%-4d | %-5s | %-30s | %6.0f%% | Cấp %-4d | [   ]\n",
                    stt, comp.getCode(), comp.getName(), weightPercent, req.getRequiredLevel()));

            // Tạo chi tiết tiêu chí đánh giá
            StringBuilder qBuilder = new StringBuilder();
            qBuilder.append(String.format("\n%d. %s (Mã: %s - Trọng số: %.0f%%):\n", stt, comp.getName(), comp.getCode(), weightPercent));
            qBuilder.append(String.format("   * Hành vi chuẩn cấp %d: %s\n", req.getRequiredLevel(), levelDesc));
            qBuilder.append("   * Nhận xét ứng viên: ....................................................................\n");
            qBuilder.append("   * Điểm đánh giá (1 - 5): [   ]");
            questionsSection.add(qBuilder.toString());

            stt++;
        }

        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format("Tổng trọng số kiểm tra: %.0f%% (Yêu cầu bằng 100%%)\n", totalWeight * 100));
        sb.append("================================================================================\n");
        sb.append("\nCHI TIẾT ĐÁNH GIÁ & GỢI Ý HÀNH VI CẦN QUAN SÁT:\n");
        for (String q : questionsSection) {
            sb.append(q).append("\n");
        }

        sb.append("\n================================================================================\n");
        sb.append("KẾT LUẬN PHỎNG VẤN:\n");
        sb.append("[ ] ĐẠT (Đề xuất nhận việc)    [ ] CẦN CÂN NHẮC    [ ] KHÔNG ĐẠT\n");
        sb.append("Tổng điểm quy đổi theo trọng số: ....... / 5.0\n");
        sb.append("Ghi chú chung: ....................................................................................\n");
        sb.append("Chữ ký người phỏng vấn: ..........................\n");
        sb.append("================================================================================\n");

        return sb.toString();
    }

    // ==========================================
    // 5. HAM MAIN CHẠY THỬ NGHIỆM
    // ==========================================
    public static void main(String[] args) {
        CompetencyFrameworkSystem system = new CompetencyFrameworkSystem();

        // 1. Khởi tạo Khung năng lực
        Competency c01 = new Competency("C01", "Giao tiếp & Truyền đạt", "CORE");
        c01.addLevel(1, "Trình bày rõ ràng ý kiến cá nhân trong phạm vi công việc.");
        c01.addLevel(2, "Lắng nghe chủ động, viết báo cáo mạch lạc.");
        c01.addLevel(3, "Thuyết phục được đối tác/đồng nghiệp trong các cuộc họp.");
        c01.addLevel(4, "Đàm phán hiệu quả các vấn đề phức tạp.");
        system.registerCompetency(c01);

        Competency c02 = new Competency("C02", "Tư duy Giải quyết vấn đề", "CORE");
        c02.addLevel(1, "Nhận biết sự cố và báo cáo cấp trên.");
        c02.addLevel(2, "Phân tích nguyên nhân gốc rễ đơn giản và đề xuất giải pháp.");
        c02.addLevel(3, "Xử lý sự cố phức tạp phát sinh trong vận hành.");
        c02.addLevel(4, "Dự báo rủi ro và xây dựng phương án phòng ngừa.");
        system.registerCompetency(c02);

        Competency f01 = new Competency("F01", "Lập trình & Phát triển phần mềm", "FUNCTIONAL");
        f01.addLevel(1, "Viết code đơn giản dưới sự hướng dẫn, hiểu cú pháp cơ bản.");
        f01.addLevel(2, "Tự phát triển feature nhỏ, viết unit test.");
        f01.addLevel(3, "Thiết kế module, tối ưu hóa hiệu năng, code review.");
        f01.addLevel(4, "Thiết kế kiến trúc hệ thống (System Architecture).");
        system.registerCompetency(f01);

        Competency m02 = new Competency("M02", "Lãnh đạo & Phát triển con người", "MANAGEMENT");
        m02.addLevel(1, "Hỗ trợ, hướng dẫn đồng nghiệp mới.");
        m02.addLevel(2, "Kèm cặp (Mentoring) nhân viên Junior.");
        m02.addLevel(3, "Đánh giá hiệu suất, đào tạo phát triển đội ngũ.");
        system.registerCompetency(m02);

        // 2. Khởi tạo Danh mục Chức danh
        JobTitle devSenior = new JobTitle("DEV-SEN", "Lập trình viên (Senior)", "Senior");
        system.registerJobTitle(devSenior);

        // 3. Ánh xạ Năng lực, Cấp độ yêu cầu & Trọng số cho vị trí Senior Developer
        List<CompetencyRequirement> seniorReqs = Arrays.asList(
            new CompetencyRequirement("C01", 3, 0.15), // 15%
            new CompetencyRequirement("C02", 3, 0.20), // 20%
            new CompetencyRequirement("F01", 4, 0.50), // 50%
            new CompetencyRequirement("M02", 2, 0.15)  // 15%
        );
        system.registerJobTitle(devSenior);
        system.mapCompetencyToJob("DEV-SEN", seniorReqs);

        // 4. Sinh và in Phiếu đánh giá phỏng vấn
        String interviewSheet = system.generateInterviewSheet(
            "DEV-SEN", 
            "Nguyễn Văn A", 
            "Trần Văn B (Tech Lead)", 
            "21/10/2026"
        );

        System.out.println(interviewSheet);
    }
}